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
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.CreateLobbyPacket;

import java.util.HashMap;
import java.util.Map;

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

            Map<Role, Integer> roles = new HashMap<>();
            roles.put(Role.MAFIA, 2);
            roles.put(Role.CITIZEN, 3);

            f.channel().writeAndFlush(
                    new CreateLobbyPacket("Test",
                            SessionInitialParameters.builder()
                                    .playersCount(5)
                                    .rolesCount(roles)
                                    .build()));
            Thread.sleep(5000);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
