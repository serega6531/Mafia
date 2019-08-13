package ru.serega6531.mafia.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.serega6531.mafia.packets.MafiaPacket;

public class MafiaServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final MafiaPacket packet = (MafiaPacket) msg;
        System.out.println(packet.getName());
        ctx.channel().writeAndFlush(new MafiaPacket(packet.getName() + "111"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
