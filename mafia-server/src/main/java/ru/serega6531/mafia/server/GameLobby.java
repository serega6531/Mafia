package ru.serega6531.mafia.server;

import lombok.Data;
import lombok.NonNull;
import ru.serega6531.mafia.SessionInitialParameters;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameLobby {

    private final int id;  // станет id GameSession после начала раунда

    @NonNull
    private String creator;

    private SessionInitialParameters parameters;

    private List<String> players;

    public GameLobby(int id, String creator, SessionInitialParameters parameters) {
        this.id = id;
        this.creator = creator;
        this.parameters = parameters;

        players = new ArrayList<>(parameters.getPlayersCount());
    }

}
