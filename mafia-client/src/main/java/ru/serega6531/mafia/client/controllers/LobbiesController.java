package ru.serega6531.mafia.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;

import java.util.HashMap;
import java.util.Map;
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

    private ObservableList<GameLobby> observableLobbiesList;

    @FXML
    public void initialize() {
        // mock:
        Map<Role, Integer> roles = new HashMap<>();
        roles.put(Role.MAFIA, 2);
        roles.put(Role.CITIZEN, 3);

        final GameLobby lobby1 = new GameLobby(1, "serega6531",
                SessionInitialParameters.builder()
                        .playersCount(5)
                        .rolesCount(roles)
                        .build());

        final GameLobby lobby2 = new GameLobby(2, "144th",
                SessionInitialParameters.builder()
                        .playersCount(5)
                        .rolesCount(roles)
                        .build());

        playerNameLabel.setText("Test");

        observableLobbiesList = FXCollections.observableArrayList(lobby1, lobby2);
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

    public void onJoinClick(ActionEvent e) {

    }

    public void onCreateLobbyClick(ActionEvent e) {

    }
}
