package ru.serega6531.mafia.server.session;

import java.util.TimerTask;

public class MafiaTimerTask extends TimerTask {
    @Override
    public void run() {
        System.out.println("Timer task");
    }
}
