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
import ru.serega6531.mafia.client.controllers.WinScreenController;
import ru.serega6531.mafia.enums.LobbyUpdateType;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.packets.server.*;

import java.util.Optional;
import java.util.function.Consumer;

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
            Consumer<LobbyUpdatedPacket> listener = MafiaClient.getLobbyUpdateConsumer();
            if (listener != null) {
                listener.accept(update);
            }
        } else if (packet instanceof ChatMessagePacket) {
            Consumer<ChatMessagePacket> listener = MafiaClient.getChatMessageConsumer();
            if (listener != null) {
                listener.accept((ChatMessagePacket) packet);
            }
        } else if (packet instanceof InformationMessagePacket) {
            final Consumer<InformationMessagePacket> listener = MafiaClient.getInformationMessageConsumer();
            if (listener != null) {
                listener.accept((InformationMessagePacket) packet);
            }
        } else if (packet instanceof SessionStartedPacket) {
            SessionStartedPacket startedPacket = (SessionStartedPacket) packet;
            if (MafiaClient.getCurrentLobby() != null &&
                    startedPacket.getSessionId() == MafiaClient.getCurrentLobby().getId()) {
                MafiaClient.setCurrentLobby(null);
                MafiaClient.setCurrentSession(new LocalSession(
                        startedPacket.getSessionId(),
                        startedPacket.getParameters(),
                        startedPacket.getPlayerNumber(),
                        startedPacket.getPlayers(),
                        startedPacket.getKnownRoles()));

                Parent root = FXMLLoader.load(getClass().getResource("/game.fxml"));
                final Stage primaryStage = MafiaClient.getPrimaryStage();
                Scene scene = new Scene(root);

                Platform.runLater(() -> primaryStage.setScene(scene));
            }
        } else if (packet instanceof GameEndedPacket) {
            if (MafiaClient.getCurrentSession() != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/results.fxml"));
                final GameEndedPacket gameEndedPacket = (GameEndedPacket) packet;
                Parent root = loader.load();
                final WinScreenController controller = loader.getController();
                controller.init(gameEndedPacket.getAllRoles(), gameEndedPacket.getReason());
                final Stage primaryStage = MafiaClient.getPrimaryStage();
                Scene scene = new Scene(root);

                Platform.runLater(() -> primaryStage.setScene(scene));
            }
        } else if (packet instanceof CountdownPacket) {
            final Consumer<CountdownPacket> listener = MafiaClient.getCountdownConsumer();
            if (listener != null) {
                listener.accept((CountdownPacket) packet);
            }
        } else if (packet instanceof StartVotingPacket) {
            final Consumer<StartVotingPacket> listener = MafiaClient.getStartVotingListener();
            if (listener != null) {
                listener.accept((StartVotingPacket) packet);
            }
        } else if (packet instanceof StopVotingPacket) {
            final Runnable listener = MafiaClient.getStopVotingListener();
            if (listener != null) {
                listener.run();
            }
        } else if (packet instanceof VoteResultsPacket) {
            final Consumer<VoteResultsPacket> listener = MafiaClient.getVoteResultsListener();
            if (listener != null) {
                listener.accept((VoteResultsPacket) packet);
            }
        } else if (packet instanceof PlayerDiedPacket) {
            final Consumer<PlayerDiedPacket> listener = MafiaClient.getPlayerDiedListener();
            if (listener != null) {
                listener.accept((PlayerDiedPacket) packet);
            }
        } else if (packet instanceof RoleRevealPacket) {
            final Consumer<RoleRevealPacket> listener = MafiaClient.getRoleRevealListener();
            if(listener != null) {
                listener.accept((RoleRevealPacket) packet);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
