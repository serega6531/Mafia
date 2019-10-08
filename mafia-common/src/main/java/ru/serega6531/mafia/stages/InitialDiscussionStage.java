package ru.serega6531.mafia.stages;

public class InitialDiscussionStage implements GameStage {
    @Override
    public int length() {
        return 30;
    }

    @Override
    public boolean isOneOff() {
        return true;
    }

    @Override
    public String messageAtStart() {
        return "Игроки знакомятся друг с другом.";
    }
}
