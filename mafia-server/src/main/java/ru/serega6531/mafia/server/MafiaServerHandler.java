package ru.serega6531.mafia.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.packets.client.CreateLobbyPacket;
import ru.serega6531.mafia.packets.client.JoinLobbyPacket;
import ru.serega6531.mafia.packets.client.StartSessionPacket;
import ru.serega6531.mafia.packets.server.ErrorMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyJoinedPacket;
import ru.serega6531.mafia.server.exceptions.IllegalSessionStateException;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.server.session.SessionsService;

public class MafiaServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelGroup clients;
    private final SessionsService sessionsService = new SessionsService();

    public MafiaServerHandler(ChannelGroup clients) {
        this.clients = clients;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final MafiaPacket packet = (MafiaPacket) msg;
        System.out.println(msg);

        if (packet instanceof CreateLobbyPacket) {
            try {
                CreateLobbyPacket createSessionPacket = ((CreateLobbyPacket) packet);
                final SessionInitialParameters parameters = createSessionPacket.getParameters();
                final GameLobby lobby = sessionsService.createLobby(parameters, createSessionPacket.getName());

                ctx.writeAndFlush(new LobbyJoinedPacket(lobby));
            } catch (IllegalSessionStateException e) {
                ctx.writeAndFlush(new ErrorMessagePacket(e.getMessage()));
            }
        } else if(packet instanceof JoinLobbyPacket) {
            //TODO
        } else if(packet instanceof StartSessionPacket) {
            //TODO
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        clients.add(channel);
        System.out.println("Клиент подключился: " + channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        System.out.println("Клиент отключился: " + channel.remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
