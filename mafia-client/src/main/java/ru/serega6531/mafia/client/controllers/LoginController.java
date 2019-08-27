package ru.serega6531.mafia.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.packets.client.LoginPacket;

import java.util.concurrent.ThreadLocalRandom;

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private TextField ipEdit;

    @FXML
    private TextField loginEdit;

    public void onLoginButtonClick() throws InterruptedException {
        String login = loginEdit.getText();
        if(login.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка");
            alert.setContentText("Ник не может быть пустым");

            alert.showAndWait();
            return;
        }

        loginButton.setDisable(true);

        try {
            MafiaClient.connect(ipEdit.getText());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка");
            alert.setContentText(e.getLocalizedMessage());

            alert.showAndWait();
            loginButton.setDisable(false);
            return;
        }

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        byte[] handshake = new byte[8];
        rand.nextBytes(handshake);
        MafiaClient.getChannel().writeAndFlush(new LoginPacket(new AuthData(login, handshake)));
    }


}
