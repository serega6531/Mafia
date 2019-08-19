package ru.serega6531.mafia.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.client.MafiaClient;

import java.io.IOException;
import java.util.stream.Collectors;

public class LobbiesController {

    @FXML
    private ListView<String> rolesList;

    @FXML
    private ListView<String> playersList;

    @FXML
    private Button joinButton;

    @FXML
    private ListView<GameLobby> lobbiesList;

    @FXML
    private Label playerNameLabel;

    @FXML
    public void initialize() {
        playerNameLabel.setText(MafiaClient.getAuthData().getName());

        final ObservableList<GameLobby> observableLobbiesList = MafiaClient.getObservableLobbiesList();
        lobbiesList.setItems(observableLobbiesList);

        lobbiesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            joinButton.setDisable(false);

            rolesList.setItems(FXCollections.observableArrayList(
                    newValue.getParameters().getRolesCount().entrySet()
                            .stream()
                            .map(ent -> ent.getValue() + "x " + ent.getKey().getRoleName())
                            .collect(Collectors.toList())));

            playersList.setItems(FXCollections.observableArrayList(newValue.getPlayers()));
        });
    }

    public void onJoinClick() {
        final GameLobby lobby = lobbiesList.getSelectionModel().getSelectedItem();

    }

    public void onCreateLobbyClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/creation.fxml"));
        final Stage primaryStage = MafiaClient.getPrimaryStage();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
    }
}
