package ru.serega6531.mafia.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.packets.client.ClientChatMessagePacket;
import ru.serega6531.mafia.packets.client.StartSessionPacket;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;

import java.util.stream.Collectors;

public class LobbyController {

    @FXML
    private Label rolesLabel;

    @FXML
    private Button startButton;

    @FXML
    private TextArea chatTextBox;

    @FXML
    private TextField chatInputField;

    @FXML
    private ListView<String> playersList;

    private GameLobby currentLobby;

    private ObservableList<String> observablePlayersList;

    @FXML
    public void initialize() {
        currentLobby = MafiaClient.getCurrentLobby();

        observablePlayersList = FXCollections.observableArrayList();
        observablePlayersList.addAll(currentLobby.getPlayers());
        playersList.setItems(observablePlayersList);

        final String roles = currentLobby.getParameters().getRolesCount().entrySet()
                .stream()
                .map(ent -> ent.getValue() + " " + ent.getKey().getRoleName())
                .collect(Collectors.joining(", "));
        rolesLabel.setText(roles);

        MafiaClient.setLobbyUpdateConsumer(this::lobbyUpdateListener);
        MafiaClient.setChatMessageConsumer(this::chatMessageListener);
    }

    public void onSendButtonClick() {
        final String message = chatInputField.getText();
        chatInputField.setText("");
        MafiaClient.getChannel().writeAndFlush(new ClientChatMessagePacket(MafiaClient.getAuthData(), message));
    }

    @SneakyThrows
    private void lobbyUpdateListener(LobbyUpdatedPacket update) {
        if (update.getLobby().getId() == currentLobby.getId()) {
            currentLobby = update.getLobby();

            switch (update.getType()) {
                case PLAYER_JOINED:
                    if(!update.getPlayer().equals(MafiaClient.getAuthData().getName())) {
                        Platform.runLater(() -> {
                            observablePlayersList.add(update.getPlayer());
                            chatTextBox.appendText("Присоединился игрок " + update.getPlayer() + "\n");
                            enablePlayButtonIfRequired();
                        });
                    }
                    break;
                case PLAYER_LEFT:
                    Platform.runLater(() -> {
                        observablePlayersList.remove(update.getPlayer());
                        chatTextBox.appendText("Вышел игрок " + update.getPlayer() + "\n");
                        enablePlayButtonIfRequired();
                    });
                    break;
                case LOBBY_REMOVED:
                    Platform.runLater(() -> {
                        final Stage primaryStage = MafiaClient.getPrimaryStage();
                        primaryStage.setScene(MafiaClient.getLobbiesListScene());
                    });
                    break;
                case CREATOR_CHANGED:
                    Platform.runLater(() -> {
                        chatTextBox.appendText(update.getPlayer() + " стал новым создателем лобби\n");
                        enablePlayButtonIfRequired();
                    });
                    break;
            }
        }
    }

    private void enablePlayButtonIfRequired() {
        startButton.setDisable(   // включаем только если мы создатель и достаточно игроков
                !currentLobby.getCreator().equals(MafiaClient.getAuthData().getName()) ||
                        currentLobby.getPlayers().size() !=
                                currentLobby.getParameters().getPlayersCount());
    }

    private void chatMessageListener(ChatMessagePacket packet) {
        final String message = packet.getMessage();
        final int playerNum = packet.getPlayerNum();
        String player = currentLobby.getPlayers().get(playerNum);

        chatTextBox.appendText(player + ": " + message + "\n");
    }

    public void onStartPress() {
        MafiaClient.getChannel().writeAndFlush(new StartSessionPacket(MafiaClient.getAuthData()));
    }
}
