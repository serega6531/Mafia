package ru.serega6531.mafia.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.ReadOnlyStyledDocumentBuilder;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.client.LocalSession;
import ru.serega6531.mafia.client.MafiaClient;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.packets.client.ClientChatMessagePacket;
import ru.serega6531.mafia.packets.server.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private PlayerBoxController[] playerControllers;

    @FXML
    public void initialize() throws IOException {
        currentSession = MafiaClient.getCurrentSession();
        playerControllers = new PlayerBoxController[currentSession.getPlayers().size()];

        MafiaClient.setChatMessageConsumer(this::chatMessageListener);
        MafiaClient.setInformationMessageConsumer(this::informationMessageListener);
        MafiaClient.setCountdownConsumer(this::countdownListener);
        MafiaClient.setStartVotingListener(this::startVotingListener);
        MafiaClient.setStopVotingListener(this::stopVotingListener);
        MafiaClient.setVoteResultsListener(this::voteResultsListener);
        MafiaClient.setPlayerDiedListener(this::playerDiedListener);
        MafiaClient.setLobbyUpdateConsumer(null);

        final Map<Integer, Role> roles = currentSession.getKnownRoles().stream()
                .collect(Collectors.toMap(RoleInfo::getPlayerNum, RoleInfo::getRole));

        for (int i = 0; i < currentSession.getPlayers().size(); i++) {
            final String playerName = currentSession.getPlayers().get(i);
            boolean isCurrentPlayer = i == currentSession.getPlayerNumber();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/playerBox.fxml"));

            final PlayerBoxController controller = new PlayerBoxController(this, playerName, i, isCurrentPlayer);
            playerControllers[i] = controller;

            loader.setController(controller);
            Pane playerBox = loader.load();
            controller.setKnownRole(roles.getOrDefault(i, Role.UNKNOWN));

            playersPane.getChildren().add(playerBox);
        }
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

        appendColoredText(Arrays.asList(player, ": ", message), Arrays.asList("red", "black", "blue"));
    }

    private void informationMessageListener(InformationMessagePacket packet) {
        final String message = packet.getMessage();

        appendColoredText(Collections.singletonList(message), Collections.singletonList("blue"));
    }

    private void countdownListener(CountdownPacket packet) {
        Platform.runLater(() -> {
            timerLabel.setText(String.valueOf(packet.getSecondsLeft()));
        });
    }

    private void startVotingListener(StartVotingPacket packet) {
        final List<Integer> possiblePlayers = packet.getPossiblePlayers();
        Platform.runLater(() -> {
            for (int player : possiblePlayers) {
                playerControllers[player].showVoteButton(true);
            }
        });
    }

    void stopVotingListener() {
        Platform.runLater(() -> {
            for (PlayerBoxController playerController : playerControllers) {
                playerController.showVoteButton(false);
            }
        });
    }

    private void voteResultsListener(VoteResultsPacket packet) {
        for (int player = 0; player < packet.getVotesForPlayers().length; player++) {
            List<String> texts = Arrays.asList(
                    "За ", currentSession.getPlayers().get(player), " - ",
                    String.valueOf(packet.getVotesForPlayers()[player]), " голосов");
            List<String> colors = Arrays.asList("black", "blue", "black", "blue", "black");
            appendColoredText(texts, colors);
        }
    }

    private void playerDiedListener(PlayerDiedPacket packet) {
        Platform.runLater(() -> playerControllers[packet.getPlayerIndex()].setDeathReason(packet.getReason()));
    }

    private void appendColoredText(List<String> textParts, List<String> colors) {
        final StyleSpansBuilder<String> stylesBuilder = new StyleSpansBuilder<>();
        final String fullText = String.join("", textParts) + "\n";

        for (int i = 0; i < textParts.size(); i++) {
            String text = textParts.get(i);
            String color = colors.get(i);

            stylesBuilder.add(
                    new StyleSpan<>(String.format("-fx-font-size: 14pt; -fx-fill: %s;", color),
                            text.length() + (i == textParts.size() - 1 ? 1 : 0)));
        }

        Platform.runLater(() -> chatTextBox.append(
                new ReadOnlyStyledDocumentBuilder<String, String, String>(SegmentOps.styledTextOps(), null)
                        .addParagraph(fullText, stylesBuilder.create())
                        .build()));
    }
}
