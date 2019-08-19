package ru.serega6531.mafia.client.controllers;

import io.netty.channel.Channel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.CreateLobbyPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LobbyCreationController {

    @FXML
    public Button cancelButton;

    @FXML
    public Button submitButton;

    @FXML
    private Spinner<Integer> playersSpinner;

    @FXML
    private Spinner<Integer> innocentsSpinner;

    @FXML
    private Spinner<Integer> mafiaSpinner;

    private boolean updatingField;  // костыли, чтобы при изменении значения в поле не срабатывал ивент

    @FXML
    public void initialize() {
        playersSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 11, 5));
        innocentsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 4, 4));
        mafiaSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));

        playersSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int maxMafia = newValue / 2 - (newValue % 2 == 0 ? 1 : 0);
            int maxInnocents = newValue - 1;
            int minInnocents = newValue / 2 + 1;

            int recommendedMafia = Math.max(newValue / 3, 1);

            final SpinnerValueFactory.IntegerSpinnerValueFactory innocentsFactory =
                    (SpinnerValueFactory.IntegerSpinnerValueFactory) innocentsSpinner.getValueFactory();
            final SpinnerValueFactory.IntegerSpinnerValueFactory mafiaFactory =
                    (SpinnerValueFactory.IntegerSpinnerValueFactory) mafiaSpinner.getValueFactory();

            innocentsFactory.maxProperty().set(maxInnocents);
            innocentsFactory.minProperty().set(minInnocents);
            innocentsFactory.setValue(newValue - recommendedMafia);
            mafiaFactory.maxProperty().set(maxMafia);
            mafiaFactory.setValue(recommendedMafia);
        });

        innocentsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(updatingField) {
                return;
            }

            final SpinnerValueFactory<Integer> mafiaFactory = mafiaSpinner.getValueFactory();
            updatingField = true;
            mafiaFactory.setValue(playersSpinner.getValue() - innocentsSpinner.getValue());
            updatingField = false;
        });

        mafiaSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(updatingField) {
                return;
            }

            final SpinnerValueFactory<Integer> innocentsFactory = innocentsSpinner.getValueFactory();
            updatingField = true;
            innocentsFactory.setValue(playersSpinner.getValue() - mafiaSpinner.getValue());
            updatingField = false;
        });
    }

    public void onCancelClick() throws IOException {
        final Stage primaryStage = MafiaClient.getPrimaryStage();
        primaryStage.setScene(MafiaClient.getLobbiesListScene());
    }

    public void onSubmitClick() {
        int totalPlayers = playersSpinner.getValue();
        int innocents = innocentsSpinner.getValue();
        int mafia = mafiaSpinner.getValue();

        Map<Role, Integer> roles = new HashMap<>();
        roles.put(Role.MAFIA, mafia);
        roles.put(Role.CITIZEN, innocents);

        final SessionInitialParameters initialParameters = SessionInitialParameters.builder()
                .playersCount(totalPlayers)
                .rolesCount(roles)
                .build();

        final Channel channel = MafiaClient.getChannel();
        channel.writeAndFlush(new CreateLobbyPacket(MafiaClient.getAuthData(), initialParameters));

        submitButton.setDisable(true);
        cancelButton.setDisable(true);
    }
}
