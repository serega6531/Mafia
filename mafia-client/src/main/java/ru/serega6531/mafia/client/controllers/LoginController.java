package ru.serega6531.mafia.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.serega6531.mafia.client.MafiaClient;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField loginEdit;

    public void onLoginButtonClick(ActionEvent e) throws IOException {
        String login = loginEdit.getText();
        if(login.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка");
            alert.setContentText("Ник не может быть пустым");

            alert.showAndWait();
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/lobbies.fxml"));
        final Stage primaryStage = MafiaClient.getPrimaryStage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/lobbies.css").toExternalForm());
        primaryStage.setScene(scene);
    }
}
