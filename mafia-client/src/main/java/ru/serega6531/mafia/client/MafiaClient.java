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
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.LogoutPacket;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        MafiaClient.primaryStage = primaryStage;

//        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));

        //Testing:
        authData = new AuthData("serega6531", new byte[0]);

        Map<Role, Integer> roles = new HashMap<>();
        roles.put(Role.MAFIA, 2);
        roles.put(Role.CITIZEN, 3);

        final SessionInitialParameters parameters = SessionInitialParameters.builder()
                .playersCount(5)
                .rolesCount(roles)
                .build();

        currentSession = new LocalSession(1, parameters, 0,
                Arrays.asList("serega6531", "test1", "test2", "test3", "test4"),
                Arrays.asList(
                        new RoleInfo(0, Role.MAFIA),
                        new RoleInfo(2, Role.MAFIA),
                        new RoleInfo(3, Role.CITIZEN)));
        Parent root = FXMLLoader.load(getClass().getResource("/game.fxml"));
        //End testing

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
