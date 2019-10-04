package ru.serega6531.mafia.stages;

public class DayDiscussionStage implements GameStage {
    @Override
    public int length() {
        return 30;
    }

    @Override
    public String messageAtStart() {
        return "Наступил день. Игроки начинают обсуждение.";
    }
}
