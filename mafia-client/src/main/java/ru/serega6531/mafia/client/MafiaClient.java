package ru.serega6531.mafia.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.serega6531.mafia.packets.MafiaPacket;

public class MafiaClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline()
                            .addLast(new ObjectEncoder())
                            .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                            .addLast(new MafiaClientHandler());
                }
            });

            ChannelFuture f = b.connect("localhost", 1111).sync();

            f.channel().writeAndFlush(new MafiaPacket("Test")).sync();
            f.channel().close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
