package ru.serega6531.mafia.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.LobbyUpdateType;
import ru.serega6531.mafia.packets.client.*;
import ru.serega6531.mafia.packets.server.*;
import ru.serega6531.mafia.server.exceptions.MafiaErrorMessageException;
import ru.serega6531.mafia.server.session.GameSession;
import ru.serega6531.mafia.server.session.SessionsService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@ChannelHandler.Sharable  // нужно следить за race condition
public class MafiaServerHandler extends ChannelInboundHandlerAdapter {

    @Getter
    private ChannelGroup allClients;
    private BiMap<String, Channel> channelsForPlayers = HashBiMap.create();

    private Map<String, byte[]> handshakes = new HashMap<>();

    private final SessionsService sessionsService = new SessionsService(this);

    public MafiaServerHandler(ChannelGroup allClients) {
        this.allClients = allClients;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ClientSidePacket)) {
            return;
        }

        final ClientSidePacket packet = (ClientSidePacket) msg;
        System.out.println(msg);
        final AuthData authData = packet.getAuthData();
        String player = authData.getName();

        if (packet instanceof LoginPacket) {
            if (channelsForPlayers.containsKey(player)) {
                ctx.writeAndFlush(new ErrorMessagePacket("Такой игрок уже есть на сервере"));
            }

            final byte[] initial = authData.getHandshake();

            if (initial.length != 8) {
                ctx.writeAndFlush(new ErrorMessagePacket("Неверная авторизация"));
                return;
            }

            final ThreadLocalRandom rand = ThreadLocalRandom.current();
            byte[] handshake = new byte[8];
            rand.nextBytes(handshake);

            for (int i = 0; i < 8; i++) {
                handshake[i] ^= initial[i];
            }

            handshakes.put(player, handshake);
            channelsForPlayers.put(player, ctx.channel());

            ctx.writeAndFlush(new LoginResponsePacket(new AuthData(player, handshake),
                    sessionsService.getAllLobbies()));
            return;
        }

        if (!channelsForPlayers.containsKey(player) || !handshakes.containsKey(player) ||
                !Arrays.equals(handshakes.get(player), authData.getHandshake())) {
            ctx.writeAndFlush(new ErrorMessagePacket("Вы не авторизированы"));
            return;
        }

        try {
            if (packet instanceof LogoutPacket) {
                final GameLobby lobby = sessionsService.getLobbyByPlayer(player);
                final GameSession session = sessionsService.getSessionByPlayer(player);

                sessionsService.removePlayer(player, ctx.channel());

                if (lobby != null) {
                    final ChannelGroup group = sessionsService.getChannelGroup(lobby.getId());
                    lobby.getPlayers().remove(player);

                    if (!lobby.getPlayers().isEmpty()) {
                        if (lobby.getCreator().equals(player)) {
                            lobby.setCreator(lobby.getPlayers().get(0));

                            group.write(new LobbyUpdatedPacket(
                                    LobbyUpdateType.CREATOR_CHANGED, lobby.getCreator(), lobby));
                        }

                        group.writeAndFlush(new LobbyUpdatedPacket(
                                LobbyUpdateType.PLAYER_LEFT, player, lobby));
                    } else {
                        sessionsService.removeLobby(lobby);

                        allClients.writeAndFlush(new LobbyUpdatedPacket(
                                LobbyUpdateType.LOBBY_REMOVED, null, lobby));
                    }
                } else if (session != null) {
                    //TODO
                }

                handshakes.remove(player);
                channelsForPlayers.remove(player);
            } else if (packet instanceof CreateLobbyPacket) {
                CreateLobbyPacket createSessionPacket = ((CreateLobbyPacket) packet);
                final SessionInitialParameters parameters = createSessionPacket.getParameters();
                final GameLobby lobby = sessionsService.createLobby(parameters, player, ctx.channel());

                ctx.write(new LobbyJoinedPacket(lobby));
                allClients.writeAndFlush(new LobbyUpdatedPacket(LobbyUpdateType.LOBBY_CREATED, null, lobby));
            } else if (packet instanceof JoinLobbyPacket) {
                final GameLobby lobby = sessionsService.joinLobby(player, ((JoinLobbyPacket) packet).getLobbyId(), ctx.channel());

                if (lobby != null) {
                    ctx.write(new LobbyJoinedPacket(lobby));
                    allClients.writeAndFlush(new LobbyUpdatedPacket(LobbyUpdateType.PLAYER_JOINED, player, lobby));
                }
            } else if (packet instanceof StartSessionPacket) {
                final GameSession session = sessionsService.startSession(player);

                final List<String> playersNames = Arrays.stream(session.getPlayers())
                        .map(GamePlayer::getVisibleName)
                        .collect(Collectors.toList());

                for (GamePlayer gp : session.getPlayers()) {
                    final Channel channel = channelsForPlayers.get(gp.getName());
                    channel.writeAndFlush(new SessionStartedPacket(
                            session.getId(),
                            session.getParameters(),
                            gp.getNumber(),
                            playersNames,
                            gp.getKnownRoles()));
                }
            } else if (packet instanceof ClientChatMessagePacket) {
                String message = ((ClientChatMessagePacket) packet).getMessage();
                message = message.trim();

                if(message.isEmpty()) {
                    return;
                }

                final GameLobby lobby = sessionsService.getLobbyByPlayer(player);
                if (lobby != null) {
                    final ChatMessagePacket outMsg = new ChatMessagePacket(lobby.getPlayers().indexOf(player), message, ChatMessagePacket.ChatChannel.GLOBAL);
                    sessionsService.getChannelGroup(lobby.getId()).writeAndFlush(outMsg);
                    return;
                }

                final GameSession session = sessionsService.getSessionByPlayer(player);
                if (session != null) {
                    GamePlayer gp = session.getPlayerByName(player);
                    session.handleChatMessage(gp, message);
                }
            } else if (packet instanceof PlayerVotePacket) {
                PlayerVotePacket votePacket = (PlayerVotePacket) packet;
                final GameSession session = sessionsService.getSessionByPlayer(player);
                if (session != null) {
                    GamePlayer gp = session.getPlayerByName(player);
                    session.handleVote(gp, votePacket.getPlayerIndex());
                }
            }
        } catch (MafiaErrorMessageException e) {
            ctx.writeAndFlush(new ErrorMessagePacket(e.getMessage()));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        allClients.add(channel);
        System.out.println("Клиент подключился: " + channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        final String player = channelsForPlayers.inverse().remove(channel);
        System.out.println("Клиент отключился: " + channel.remoteAddress() + ", " + player);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

    public Channel getChannelForPlayer(String player) {
        return channelsForPlayers.get(player);
    }

}
