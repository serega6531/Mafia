package ru.serega6531.mafia.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.packets.client.StartSessionPacket;
import ru.serega6531.mafia.packets.server.LobbyJoinedPacket;

public class MafiaClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final MafiaPacket packet = (MafiaPacket) msg;
        System.out.println(packet);

        if(packet instanceof LobbyJoinedPacket) {
            ctx.channel().writeAndFlush(new StartSessionPacket("Test", new byte[0]));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
