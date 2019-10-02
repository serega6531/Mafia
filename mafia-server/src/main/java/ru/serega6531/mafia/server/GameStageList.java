package ru.serega6531.mafia.server;

import lombok.Getter;
import ru.serega6531.mafia.server.session.stages.GameStage;

import java.util.List;

public class GameStageList {

    private final List<GameStage> internalList;

    @Getter
    private GameStage currentStage;
    private int position = 0;

    public GameStageList(List<GameStage> internalList) {
        this.internalList = internalList;
    }

    public GameStage next() {
        currentStage = internalList.get(position++);

        if(position == internalList.size()) {
            position = 0;
        }

        return currentStage;
    }

    public void remove(Class<? extends GameStage> cl) {
        for (int i = 0; i < internalList.size(); i++) {
            GameStage internalStage = internalList.get(i);
            if(internalStage.getClass() == cl) {
                if(i < position) {
                    position--;
                } else if(i == position && internalList.size() - 1 == position) {
                    position = 0;
                }

                internalList.remove(i);
                break;
            }
        }
    }

}
