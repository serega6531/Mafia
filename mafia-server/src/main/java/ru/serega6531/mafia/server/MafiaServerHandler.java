package ru.serega6531.mafia.server;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.packets.client.*;
import ru.serega6531.mafia.packets.server.ErrorMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyJoinedPacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;
import ru.serega6531.mafia.packets.server.SessionStartedPacket;
import ru.serega6531.mafia.server.exceptions.MafiaErrorMessageException;
import ru.serega6531.mafia.server.session.GameSession;
import ru.serega6531.mafia.server.session.SessionsService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MafiaServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelGroup allClients;
    private Map<String, Channel> channelsByPlayer = new HashMap<>();

    private Map<String, byte[]> handshakes = new HashMap<>();

    private final SessionsService sessionsService = new SessionsService(this);

    public MafiaServerHandler(ChannelGroup allClients) {
        this.allClients = allClients;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final ClientSidePacket packet = (ClientSidePacket) msg;
        System.out.println(msg);
        String player = packet.getName();

        if(packet instanceof LoginPacket) {
            final LoginPacket loginPacket = (LoginPacket) packet;
            final byte[] initial = loginPacket.getHandshake();

            if(initial.length != 8) {
                ctx.writeAndFlush(new ErrorMessagePacket("Неверная авторизация"));
                return;
            }

            final ThreadLocalRandom rand = ThreadLocalRandom.current();
            byte[] handshake = new byte[8];
            rand.nextBytes(handshake);

            for(int i = 0; i < 8; i++) {
                handshake[i] ^= initial[i];
            }

            handshakes.put(player, handshake);
            channelsByPlayer.put(player, ctx.channel());
            return;
        }

        if(!handshakes.containsKey(player) || !Arrays.equals(handshakes.get(player), packet.getHandshake())) {
            ctx.writeAndFlush(new ErrorMessagePacket("Вы не авторизированы"));
            return;
        }

        try {
            if (packet instanceof CreateLobbyPacket) {
                CreateLobbyPacket createSessionPacket = ((CreateLobbyPacket) packet);
                final SessionInitialParameters parameters = createSessionPacket.getParameters();
                final GameLobby lobby = sessionsService.createLobby(parameters, player);

                ctx.writeAndFlush(new LobbyJoinedPacket(lobby));
            } else if (packet instanceof JoinLobbyPacket) {
                final GameLobby lobby = sessionsService.joinLobby(player, ((JoinLobbyPacket) packet).getLobbyId());

                if(lobby != null) {
                    allClients.writeAndFlush(new LobbyUpdatedPacket(player, true, lobby));
                }
            } else if (packet instanceof StartSessionPacket) {
                final GameSession session = sessionsService.startSession(player);

                final List<String> playersNames = Arrays.stream(session.getPlayers())
                        .map(GamePlayer::getVisibleName)
                        .collect(Collectors.toList());

                for (GamePlayer gp : session.getPlayers()) {
                    final Channel channel = channelsByPlayer.get(gp.getName());
                    channel.writeAndFlush(new SessionStartedPacket(gp.getNumber(), playersNames, gp.getKnownRoles()));
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
        channelsByPlayer.values().remove(channel);
        System.out.println("Клиент отключился: " + channel.remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

    public Channel getChannelForPlayer(String player) {
        return channelsByPlayer.get(player);
    }

}
