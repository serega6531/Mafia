package ru.serega6531.mafia.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;

public class LobbyController {

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

        MafiaClient.setLobbyUpdateConsumer(this::lobbyUpdateListener);
        MafiaClient.setChatMessageConsumer(this::chatMessageListener);
    }

    public void onSendButtonClick() {
        final String message = chatInputField.getText();
        chatInputField.setText("");

    }

    private void lobbyUpdateListener(LobbyUpdatedPacket update) {
        if(update.getLobby().getId() == currentLobby.getId()) {
            currentLobby = update.getLobby();

            switch (update.getType()) {
                case PLAYER_JOINED:
                    observablePlayersList.add(update.getPlayer());
                    break;
                case PLAYER_LEFT:
                    observablePlayersList.remove(update.getPlayer());
                    break;
                case LOBBY_REMOVED:
                    //TODO
                    break;
            }
        }
    }

    private void chatMessageListener(ChatMessagePacket packet) {
        final String message = packet.getMessage();
        final int playerNum = packet.getPlayerNum();
        String player = currentLobby.getPlayers().get(playerNum);

        chatTextBox.appendText(player + ": " + message);
    }
}
