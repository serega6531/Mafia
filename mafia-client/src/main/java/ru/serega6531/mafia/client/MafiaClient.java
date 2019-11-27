package ru.serega6531.mafia.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.packets.client.LogoutPacket;
import ru.serega6531.mafia.packets.server.*;

import java.io.IOException;
import java.util.function.Consumer;

public class MafiaClient extends Application {

    @Getter
    private static Stage primaryStage;

    @Getter
    private static ObservableList<GameLobby> observableLobbiesList = FXCollections.observableArrayList();

    @Getter
    private static Channel channel;

    @Getter
    @Setter
    private static AuthData authData;

    @Getter
    @Setter
    private static Scene lobbiesListScene;

    @Getter
    @Setter
    private static GameLobby currentLobby;

    @Getter
    @Setter
    private static LocalSession currentSession;

    @Getter
    @Setter
    private static Consumer<LobbyUpdatedPacket> lobbyUpdateConsumer;

    @Getter
    @Setter
    private static Consumer<ChatMessagePacket> chatMessageConsumer;

    @Getter
    @Setter
    private static Consumer<InformationMessagePacket> informationMessageConsumer;

    @Getter
    @Setter
    private static Consumer<CountdownPacket> countdownConsumer;

    @Getter
    @Setter
    private static Consumer<StartVotingPacket> startVotingListener;

    @Getter
    @Setter
    private static Runnable stopVotingListener;

    @Getter
    @Setter
    private static Consumer<VoteResultsPacket> voteResultsListener;

    @Getter
    @Setter
    private static Consumer<PlayerDiedPacket> playerDiedListener;

    @Getter
    @Setter
    private static Consumer<RoleRevealPacket> roleRevealListener;

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        MafiaClient.primaryStage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Мафия");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (authData != null) {
            channel.writeAndFlush(new LogoutPacket(authData));
        }
        workerGroup.shutdownGracefully();
    }

    public static void connect(String ip) throws InterruptedException {
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

        ChannelFuture f = b.connect(ip, 1111).sync();
        channel = f.channel();

//        f.channel().closeFuture().sync();
    }
}
