package ru.serega6531.mafia.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.client.LocalSession;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.packets.client.ClientChatMessagePacket;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.GameEndedPacket;

import java.util.List;

public class WinScreenController {

    @FXML
    private Label reasonLabel;

    @FXML
    private TextArea chatTextBox;

    @FXML
    private TextField chatInputField;

    @FXML
    private ListView<String> playersList;

    private LocalSession currentSession;

    @FXML
    public void initialize() {
        currentSession = MafiaClient.getCurrentSession();
        MafiaClient.setCurrentSession(null);

        MafiaClient.setChatMessageConsumer(this::chatMessageListener);
        MafiaClient.setInformationMessageConsumer(null);
        MafiaClient.setCountdownConsumer(null);
        MafiaClient.setStartVotingListener(null);
        MafiaClient.setStopVotingListener(null);
        MafiaClient.setVoteResultsListener(null);
        MafiaClient.setPlayerDiedListener(null);
        MafiaClient.setRoleRevealListener(null);
    }

    public void init(List<RoleInfo> allRoles, GameEndedPacket.Reason gameEndedReason) {
        Platform.runLater(() -> {
            reasonLabel.setText(gameEndedReason.getText());
            for (RoleInfo role : allRoles) {
                playersList.getItems().add(currentSession.getPlayers().get(role.getPlayerNum()) + " - " +
                        role.getRole().getRoleName());
            }
        });
    }

    public void onSendButtonClick(ActionEvent event) {
        final String message = chatInputField.getText();
        chatInputField.setText("");
        MafiaClient.getChannel().writeAndFlush(new ClientChatMessagePacket(MafiaClient.getAuthData(), message));
    }

    public void onQuitButtonClick(ActionEvent event) {
        MafiaClient.setChatMessageConsumer(null);
        //TODO
    }

    private void chatMessageListener(ChatMessagePacket packet) {
        final String message = packet.getMessage();
        final int playerNum = packet.getPlayerNum();
        String player = currentSession.getPlayers().get(playerNum);

        chatTextBox.appendText(player + ": " + message + "\n");
    }

}
