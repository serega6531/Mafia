package ru.serega6531.mafia.stages;

public interface GameStage {

    int length();   // в секундах

    default void prepare() {
    }

    default boolean isOneOff() {
        return false;
    }

    default String messageAtStart() {
        return null;
    }

}
