package ru.serega6531.mafia;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class GameLobby implements Serializable {

    private final int id;  // станет id GameSession после начала раунда
    private String creator;
    private final SessionInitialParameters parameters;
    private final List<String> players;

    public GameLobby(int id, String creator, SessionInitialParameters parameters) {
        this.id = id;
        this.creator = creator;
        this.parameters = parameters;

        players = new ArrayList<>(parameters.getPlayersCount());
    }

    @Override
    public String toString() {
        return String.format("Лобби %s на %d игроков", creator, parameters.getPlayersCount());
    }
}
