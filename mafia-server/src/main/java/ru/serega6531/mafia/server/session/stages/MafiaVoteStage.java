package ru.serega6531.mafia.server.session.stages;

import java.util.HashMap;
import java.util.Map;

public class MafiaVoteStage implements GameStage {

    private transient Map<String, Integer> votes = new HashMap<>();

    @Override
    public int length() {
        return 10;
    }

    @Override
    public void prepare() {
        votes.clear();
    }
}
