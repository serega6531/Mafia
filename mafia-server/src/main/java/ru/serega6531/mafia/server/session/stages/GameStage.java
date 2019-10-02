package ru.serega6531.mafia.server.session.stages;

public interface GameStage {

    int length();   // в секундах

    default void prepare() {
    }

    default boolean isOneOff() {
        return false;
    }

}
