package ru.serega6531.mafia.server;

import lombok.Data;
import lombok.NonNull;
import ru.serega6531.mafia.SessionInitialParameters;

@Data
public class GameSession {

    private final int id;

    @NonNull
    private String creator;

    private final GamePlayer[] players;

    public GameSession(int id, String creator, SessionInitialParameters parameters) {
        this.id = id;
        this.creator = creator;

        players = new GamePlayer[parameters.getPlayersCount()];
        //TODO
    }

}
