package ru.serega6531.mafia.stages;

import lombok.Getter;

public class LastWordsStage implements GameStage {

    @Getter
    private int playerNum;
    private String playerName;

    public LastWordsStage(int playerNum, String playerName) {
        this.playerNum = playerNum;
        this.playerName = playerName;
    }

    @Override
    public int length() {
        return 20;
    }

    @Override
    public boolean isOneOff() {
        return true;
    }

    @Override
    public String messageAtStart() {
        return playerName + " начинает свою последнюю речь";
    }
}
