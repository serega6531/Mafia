package ru.serega6531.mafia.server.session;

import ru.serega6531.mafia.packets.server.InformationMessagePacket;
import ru.serega6531.mafia.server.session.stages.GameStage;
import ru.serega6531.mafia.server.session.stages.InitialDiscussionStage;

import java.util.TimerTask;

public class MafiaTimerTask extends TimerTask {

    private final GameSession session;
    private int stageTimeLeft = 0;

    public MafiaTimerTask(GameSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        System.out.println("Timer task");
        if (stageTimeLeft == 0) {
            final GameStage stage = session.getStages().next();

            if(stage instanceof InitialDiscussionStage) {
                session.getAllPlayersChannelGroup().writeAndFlush(
                        new InformationMessagePacket("Наступил день. Игроки знакомятся друг с другом."));
            } //TODO

            if (stage.isOneOff()) {
                session.getStages().remove(stage.getClass());
            }
        }
    }
}
