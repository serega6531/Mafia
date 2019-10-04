package ru.serega6531.mafia.stages;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MafiaVoteStage implements GameStage {

    @Getter
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
