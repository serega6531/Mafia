package ru.serega6531.mafia.server.session;

import io.netty.channel.group.ChannelGroup;
import lombok.Data;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.server.GamePlayer;
import ru.serega6531.mafia.server.GameStageList;
import ru.serega6531.mafia.server.session.stages.DayVoteStage;
import ru.serega6531.mafia.server.session.stages.GameStage;
import ru.serega6531.mafia.server.session.stages.InitialDiscussionStage;
import ru.serega6531.mafia.server.session.stages.MafiaVoteStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Data
public class GameSession {

    private final int id;
    private final ChannelGroup allPlayersChannelGroup;
    private String creator;
    private final SessionInitialParameters parameters;
    private final GamePlayer[] players;

    private final GameStageList stages;
    private final Timer timer;

    public GameSession(int id, ChannelGroup allPlayersChannelGroup, String creator,
                       SessionInitialParameters parameters, GamePlayer[] players) {
        this.id = id;
        this.allPlayersChannelGroup = allPlayersChannelGroup;
        this.creator = creator;
        this.parameters = parameters;
        this.players = players;

        this.timer = new Timer("session-" + id + "-timer");
        timer.schedule(new MafiaTimerTask(this), 1000L, 1000L);

        stages = calculateGameStages();
    }

    private GameStageList calculateGameStages() {
        List<GameStage> list = new ArrayList<>();

        list.add(new InitialDiscussionStage());
        list.add(new DayVoteStage());
        list.add(new MafiaVoteStage());

        return new GameStageList(list);
    }
}
