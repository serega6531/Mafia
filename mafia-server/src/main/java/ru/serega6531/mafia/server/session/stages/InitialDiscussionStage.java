package ru.serega6531.mafia.server.session.stages;

public class InitialDiscussionStage implements GameStage {
    @Override
    public int length() {
        return 30;
    }

    @Override
    public boolean isOneOff() {
        return true;
    }
}
