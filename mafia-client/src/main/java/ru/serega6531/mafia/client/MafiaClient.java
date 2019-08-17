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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.CreateLobbyPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MafiaClient extends Application {

    @Getter
    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws InterruptedException, IOException {
        MafiaClient.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Мафия");
        primaryStage.setScene(scene);
        primaryStage.show();
//        connect();
    }

    private void connect() throws InterruptedException {
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
                            new byte[0],
                            SessionInitialParameters.builder()
                                    .playersCount(5)
                                    .rolesCount(roles)
                                    .build()));

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
