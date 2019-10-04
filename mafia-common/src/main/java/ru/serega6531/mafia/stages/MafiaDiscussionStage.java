package ru.serega6531.mafia.stages;

public class MafiaDiscussionStage implements GameStage {

    @Override
    public int length() {
        return 30;
    }

    @Override
    public String messageAtStart() {
        return "Наступила ночь. Мафия обсуждает свою цель.";
    }
}
