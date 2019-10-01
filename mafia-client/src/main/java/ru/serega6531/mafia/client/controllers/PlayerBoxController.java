package ru.serega6531.mafia.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import ru.serega6531.mafia.enums.Role;

@Getter
@Setter
public class PlayerBoxController {

    private final String playerName;
    private final int playerNumber;
    private final boolean isCurrentPlayer;

    private Role knownRole;

    @FXML
    private Label playerNameLabel;

    @FXML
    private Label playerRoleLabel;

    @FXML
    private ImageView roleImage;

    public PlayerBoxController(String playerName, int playerNumber, boolean isCurrentPlayer) {
        this.playerName = playerName;
        this.playerNumber = playerNumber;
        this.isCurrentPlayer = isCurrentPlayer;
    }

    @FXML
    public void initialize() {
        playerNameLabel.setText(playerName);
    }

    public void setKnownRole(Role knownRole) {
        this.knownRole = knownRole;
        Platform.runLater(() -> {
            roleImage.setImage(new Image("/roles/" + knownRole.name().toLowerCase() + ".png"));
            playerRoleLabel.setText("Роль: " + knownRole.getRoleName());
        });
    }
}
