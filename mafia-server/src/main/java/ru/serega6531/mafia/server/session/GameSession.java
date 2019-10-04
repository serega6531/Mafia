package ru.serega6531.mafia.server.session;

import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Team;
import ru.serega6531.mafia.packets.server.CountdownPacket;
import ru.serega6531.mafia.packets.server.InformationMessagePacket;
import ru.serega6531.mafia.packets.server.StageChangePacket;
import ru.serega6531.mafia.server.GamePlayer;
import ru.serega6531.mafia.server.GameStageList;
import ru.serega6531.mafia.stages.DayVoteStage;
import ru.serega6531.mafia.stages.GameStage;
import ru.serega6531.mafia.stages.InitialDiscussionStage;
import ru.serega6531.mafia.stages.MafiaVoteStage;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class GameSession extends TimerTask  {

    private final int id;
    private final ChannelGroup allPlayersChannelGroup;
    private String creator;
    private final SessionInitialParameters parameters;
    private final GamePlayer[] players;

    private final GameStageList stages;
    private final Timer timer;

    private int stageTimeLeft = 0;

    public GameSession(int id, ChannelGroup allPlayersChannelGroup, String creator,
                       SessionInitialParameters parameters, GamePlayer[] players) {
        this.id = id;
        this.allPlayersChannelGroup = allPlayersChannelGroup;
        this.creator = creator;
        this.parameters = parameters;
        this.players = players;

        this.timer = new Timer("session-" + id + "-timer");
        timer.schedule(this, 1000L, 1000L);

        stages = calculateGameStages();
    }

    private GameStageList calculateGameStages() {
        List<GameStage> list = new ArrayList<>();

        list.add(new InitialDiscussionStage());
        list.add(new DayVoteStage());
        list.add(new MafiaVoteStage());

        return new GameStageList(list);
    }

    @Override
    public void run() {
        System.out.println("Timer task");
        if (stageTimeLeft == 0) {
            final GameStage prevStage = stages.getCurrentStage();

            if(prevStage instanceof DayVoteStage) {
                processDayVotes(((DayVoteStage) prevStage).getVotes());
            } else if(prevStage instanceof MafiaVoteStage) {
                processMafiaVotes(((MafiaVoteStage) prevStage).getVotes());
            }

            final GameStage nextStage = stages.next();
            stageTimeLeft = nextStage.length();

            getAllPlayersChannelGroup().writeAndFlush(
                    new StageChangePacket(nextStage));

            if(nextStage.messageAtStart() != null) {
                allPlayersChannelGroup.writeAndFlush(
                        new InformationMessagePacket(nextStage.messageAtStart()));
            }

            if (nextStage.isOneOff()) {
                stages.remove(nextStage.getClass());
            }
        }

        allPlayersChannelGroup.writeAndFlush(new CountdownPacket(stageTimeLeft));

        stageTimeLeft--;
    }

    private void processDayVotes(Map<String, Integer> votesOfPlayers) {
        int[] votesForPlayers = new int[players.length];
        for (Integer vote : votesOfPlayers.values()) {
            votesForPlayers[vote]++;
        }

        final int alivePlayers = countAlivePlayers();
        if(votesOfPlayers.size() < alivePlayers) {
            votesForPlayers[findLastAlivePlayerIndex()] += alivePlayers - votesOfPlayers.size();
        }

        List<Integer> mostVoted = findMostVoted(votesForPlayers);

        if(mostVoted.size() == 1) {
            jail(mostVoted.get(0));
        } else {  // несколько кандидатов с одинаковым количеством голосов
            //TODO стадия оправдывания или посадки всех
        }
    }

    private void processMafiaVotes(Map<String, Integer> votesOfPlayers) {
        int[] votesForPlayers = new int[players.length];
        for (Integer vote : votesOfPlayers.values()) {
            votesForPlayers[vote]++;
        }

        for (int i = 0; i < players.length; i++) {
            if(players[i].getTeam() == Team.MAFIA && !votesOfPlayers.containsKey(players[i].getName())) {
                votesForPlayers[i]++;   // не проголосовавшие мафии автоматически голосуют против себя
            }
        }

        List<Integer> mostVoted = findMostVoted(votesForPlayers);

        if(mostVoted.size() == 1) {
            kill(mostVoted.get(0));
        } else {  // несколько кандидатов с одинаковым количеством голосов
            kill(mostVoted.get(ThreadLocalRandom.current().nextInt(mostVoted.size())));
        }
    }

    private void jail(int playerIndex) {

    }

    private void kill(int playerIndex) {

    }

    private List<Integer> findMostVoted(int[] votesForPlayers) {
        int maxVotes = 0;
        List<Integer> mostVoted = new ArrayList<>();

        for (int player = 0; player < votesForPlayers.length; player++) {
            int votes = votesForPlayers[player];
            if(votes > maxVotes) {
                maxVotes = votes;
                mostVoted.clear();
                mostVoted.add(player);
            } else if(votes == maxVotes) {
                mostVoted.add(player);
            }
        }

        return mostVoted;
    }

    private int countAlivePlayers() {
        return (int) Arrays.stream(players)
                .filter(GamePlayer::isAlive)
                .count();
    }

    private int findLastAlivePlayerIndex() {
        for (int i = players.length - 1; i >= 0; i--) {
            if(players[i].isAlive()) {
                return i;
            }
        }

        return 0;
    }

}
