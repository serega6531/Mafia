package ru.serega6531.mafia.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.ReadOnlyStyledDocumentBuilder;
import org.fxmisc.richtext.model.SegmentOps;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.client.LocalSession;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.ClientChatMessagePacket;
import ru.serega6531.mafia.packets.server.ChatMessagePacket;
import ru.serega6531.mafia.packets.server.InformationMessagePacket;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GameController {

    @FXML
    private Label timerLabel;
    
    @FXML
    private Pane playersPane;

    @FXML
    private InlineCssTextArea chatTextBox;

    @FXML
    private TextField chatInputField;

    private LocalSession currentSession;

    @FXML
    public void initialize() throws IOException {
        currentSession = MafiaClient.getCurrentSession();
        MafiaClient.setChatMessageConsumer(this::chatMessageListener);
        MafiaClient.setInformationMessageConsumer(this::informationMessageListener);
        MafiaClient.setLobbyUpdateConsumer(null);

        final Map<Integer, Role> roles = currentSession.getKnownRoles().stream()
                .collect(Collectors.toMap(RoleInfo::getPlayerNum, RoleInfo::getRole));

        for (int i = 0; i < currentSession.getPlayers().size(); i++) {
            final String playerName = currentSession.getPlayers().get(i);
            boolean isCurrentPlayer = i == currentSession.getPlayerNumber();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/playerBox.fxml"));

            final PlayerBoxController controller = new PlayerBoxController(playerName, i, isCurrentPlayer);
            controller.setKnownRole(roles.getOrDefault(i, Role.UNKNOWN));

            loader.setController(controller);
            Pane playerBox = loader.load();

            playersPane.getChildren().add(playerBox);
        }

        chatTextBox.append(
                new ReadOnlyStyledDocumentBuilder<String, String, String>(SegmentOps.styledTextOps(), null)
                        .addParagraph("123", "-fx-font-size: 14pt; -fx-fill: red;")
                        .build());
    }

    public void onSendButtonClick(ActionEvent event) {
        final String message = chatInputField.getText();
        chatInputField.setText("");
        MafiaClient.getChannel().writeAndFlush(new ClientChatMessagePacket(MafiaClient.getAuthData(), message));
    }

    private void chatMessageListener(ChatMessagePacket packet) {
        final String message = packet.getMessage();
        final int playerNum = packet.getPlayerNum();
        String player = currentSession.getPlayers().get(playerNum);

        final Text playerText = new Text(player);
        final Text colonText = new Text(": ");
        final Text messageText = new Text(message + "\n");
//        chatTextBox.getChildren().addAll(playerText, colonText, messageText);
//        chatTextBox.append(ReadOnlyStyledDocument.fromString(
//                "111"
//        ));
    }

    private void informationMessageListener(InformationMessagePacket packet) {
        final String message = packet.getMessage();
        final Text messageText = new Text(message + "\n");
//        chatTextBox.getChildren().add(messageText);

    }
}
