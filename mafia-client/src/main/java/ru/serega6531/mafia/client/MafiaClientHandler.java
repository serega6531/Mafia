package ru.serega6531.mafia.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.enums.LobbyUpdateType;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyJoinedPacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;
import ru.serega6531.mafia.packets.server.LoginResponsePacket;

import java.util.Optional;

public class MafiaClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final MafiaPacket packet = (MafiaPacket) msg;
        System.out.println(packet);

        if (packet instanceof LoginResponsePacket) {
            LoginResponsePacket response = ((LoginResponsePacket) packet);
            final AuthData authData = response.getAuthData();
            MafiaClient.setAuthData(authData);
            MafiaClient.getObservableLobbiesList().addAll(response.getLobbies());

            Parent root = FXMLLoader.load(getClass().getResource("/lobbiesList.fxml"));
            final Stage primaryStage = MafiaClient.getPrimaryStage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/lobbiesList.css").toExternalForm());
            MafiaClient.setLobbiesListScene(scene);

            Platform.runLater(() -> primaryStage.setScene(scene));
        } else if (packet instanceof LobbyJoinedPacket) {
            final GameLobby lobby = ((LobbyJoinedPacket) packet).getLobby();
            MafiaClient.setCurrentLobby(lobby);

            Parent root = FXMLLoader.load(getClass().getResource("/lobby.fxml"));
            final Stage primaryStage = MafiaClient.getPrimaryStage();
            Scene scene = new Scene(root);

            Platform.runLater(() -> primaryStage.setScene(scene));
        } else if (packet instanceof LobbyUpdatedPacket) {
            final LobbyUpdatedPacket update = (LobbyUpdatedPacket) packet;
            final ObservableList<GameLobby> lobbies = MafiaClient.getObservableLobbiesList();
            final GameLobby lobby = update.getLobby();

            if (update.getType() == LobbyUpdateType.LOBBY_CREATED) {
                lobbies.add(lobby);
            } else if (update.getType() == LobbyUpdateType.LOBBY_REMOVED) {
                lobbies.removeIf(l -> l.getId() == lobby.getId());
            } else if (update.getType() == LobbyUpdateType.PLAYER_JOINED ||
                    update.getType() == LobbyUpdateType.PLAYER_LEFT) {
                final Optional<GameLobby> savedLobby = lobbies.stream()
                        .filter(l -> l.getId() == lobby.getId())
                        .findAny();
                savedLobby.ifPresent(l -> {
                    l.getPlayers().clear();
                    l.getPlayers().addAll(lobby.getPlayers());
                });
            }
            MafiaClient.getLobbyUpdateConsumer().accept(update);
        } else if (packet instanceof ChatMessagePacket) {
            MafiaClient.getChatMessageConsumer().accept((ChatMessagePacket) packet);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
