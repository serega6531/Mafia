package ru.serega6531.mafia.server.session;

import lombok.Data;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.server.GamePlayer;

import java.util.Timer;

@Data
public class GameSession {

    private final int id;
    private String creator;
    private final SessionInitialParameters parameters;
    private final GamePlayer[] players;

    private Timer timer = new Timer();

    public GameSession(int id, String creator, SessionInitialParameters parameters, GamePlayer[] players) {
        this.id = id;
        this.creator = creator;
        this.parameters = parameters;
        this.players = players;

        timer.schedule(new MafiaTimerTask(), 1000L, 1000L);
    }

}
