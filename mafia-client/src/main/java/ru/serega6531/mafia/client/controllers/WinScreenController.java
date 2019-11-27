package ru.serega6531.mafia.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.client.LocalSession;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.packets.server.GameEndedPacket;

import java.io.IOException;
import java.util.List;

public class WinScreenController {

    @FXML
    private Label reasonLabel;

    @FXML
    private ListView<String> playersList;

    private LocalSession currentSession;

    @FXML
    public void initialize() {
        currentSession = MafiaClient.getCurrentSession();
        MafiaClient.setCurrentSession(null);

        MafiaClient.setChatMessageConsumer(null);
        MafiaClient.setInformationMessageConsumer(null);
        MafiaClient.setCountdownConsumer(null);
        MafiaClient.setStartVotingListener(null);
        MafiaClient.setStopVotingListener(null);
        MafiaClient.setVoteResultsListener(null);
        MafiaClient.setPlayerDiedListener(null);
        MafiaClient.setRoleRevealListener(null);
    }

    public void init(String[] realNames, List<RoleInfo> allRoles, GameEndedPacket.Reason gameEndedReason) {
        Platform.runLater(() -> {
            reasonLabel.setText(gameEndedReason.getText());
            for (RoleInfo role : allRoles) {
                playersList.getItems().add(String.format("%s (%s) - %s",
                        currentSession.getPlayers().get(role.getPlayerNum()),
                        realNames[role.getPlayerNum()],
                        role.getRole().getRoleName()));
            }
        });
    }

    public void onQuitButtonClick(ActionEvent event) throws IOException {
        MafiaClient.setChatMessageConsumer(null);
        Parent root = FXMLLoader.load(getClass().getResource("/lobbiesList.fxml"));
        final Stage primaryStage = MafiaClient.getPrimaryStage();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

}
