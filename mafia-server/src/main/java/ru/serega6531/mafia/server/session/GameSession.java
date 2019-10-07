package ru.serega6531.mafia.server.session;

import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Team;
import ru.serega6531.mafia.packets.server.*;
import ru.serega6531.mafia.server.GamePlayer;
import ru.serega6531.mafia.server.GameStageList;
import ru.serega6531.mafia.stages.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class GameSession extends TimerTask {

    private final int id;
    private final ChannelGroup allPlayersChannelGroup;
    private String creator;
    private final SessionInitialParameters parameters;
    private final GamePlayer[] players;
    private final Map<String, GamePlayer> playersByNames = new HashMap<>();

    private final GameStageList stages;
    private final Timer timer;

    private int stageTimeLeft = 0;

    private Integer playerToKillAtNight = null;

    public GameSession(int id, ChannelGroup allPlayersChannelGroup, String creator,
                       SessionInitialParameters parameters, GamePlayer[] players) {
        this.id = id;
        this.allPlayersChannelGroup = allPlayersChannelGroup;
        this.creator = creator;
        this.parameters = parameters;
        this.players = players;

        for (GamePlayer player : players) {
            playersByNames.put(player.getName(), player);
        }

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

            if (prevStage instanceof DayVoteStage) {
                processDayVotes(((DayVoteStage) prevStage).getVotes());
                allPlayersChannelGroup.writeAndFlush(new StopVotingPacket());
            } else if (prevStage instanceof MafiaVoteStage) {
                processMafiaVotes(((MafiaVoteStage) prevStage).getVotes());
                allPlayersChannelGroup.writeAndFlush(new StopVotingPacket());
            }

            final GameStage nextStage = stages.next();
            stageTimeLeft = nextStage.length();

            getAllPlayersChannelGroup().writeAndFlush(
                    new StageChangePacket(nextStage));

            if (nextStage.messageAtStart() != null) {
                allPlayersChannelGroup.writeAndFlush(
                        new InformationMessagePacket(nextStage.messageAtStart()));
            }

            if (nextStage.isOneOff()) {
                stages.remove(nextStage.getClass());
            }

            if (nextStage instanceof DayDiscussionStage && playerToKillAtNight != null) {
                final GamePlayer killed = players[playerToKillAtNight];
                allPlayersChannelGroup.write(new InformationMessagePacket(
                        "Этой ночью погиб " + killed.getVisibleName()));
                allPlayersChannelGroup.writeAndFlush(new PlayerDiedPacket(playerToKillAtNight));
                killed.setAlive(false);
                killed.getChannel().writeAndFlush(new InformationMessagePacket("Вас убили и вы выбыли из игры"));
            } else if (nextStage instanceof DayVoteStage) {
                List<Integer> possiblePlayers = getAlivePlayerIndexes();

                for (GamePlayer player : players) {
                    player.getChannel().writeAndFlush(new StartVotingPacket(possiblePlayers));
                }
            } else if (nextStage instanceof MafiaVoteStage) {
                List<Integer> possiblePlayers = getAlivePlayerIndexes();

                for (GamePlayer player : players) {
                    if(player.getTeam() == Team.MAFIA) {
                        player.getChannel().writeAndFlush(new StartVotingPacket(possiblePlayers));
                    }
                }
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
        if (votesOfPlayers.size() < alivePlayers) {
            votesForPlayers[findLastAlivePlayerIndex()] += alivePlayers - votesOfPlayers.size();
        }

        List<Integer> mostVoted = findMostVoted(votesForPlayers);

        if (mostVoted.size() == 1) {
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
            if (players[i].getTeam() == Team.MAFIA && !votesOfPlayers.containsKey(players[i].getName())) {
                votesForPlayers[i]++;   // не проголосовавшие мафии автоматически голосуют против себя
            }
        }

        List<Integer> mostVoted = findMostVoted(votesForPlayers);

        if (mostVoted.size() == 1) {
            kill(mostVoted.get(0));
        } else {  // несколько кандидатов с одинаковым количеством голосов
            kill(mostVoted.get(ThreadLocalRandom.current().nextInt(mostVoted.size())));
        }
    }

    private void jail(int playerIndex) {
        GamePlayer player = players[playerIndex];
        player.setAlive(false);
        player.getChannel().writeAndFlush(new InformationMessagePacket("Вас посадили и вы выбыли из игры"));
        allPlayersChannelGroup.writeAndFlush(new InformationMessagePacket("Посажен " + player.getVisibleName()));

        if (player.getTeam() != Team.MAFIA && checkForMafiaWin()) {
            allPlayersChannelGroup.writeAndFlush(new InformationMessagePacket("Победа мафии (TODO)"));
        } else if (player.getTeam() == Team.MAFIA && checkForInnocentsWin()) {
            allPlayersChannelGroup.writeAndFlush(new InformationMessagePacket("Победа мирных (TODO)"));
        }
    }

    private void kill(int playerIndex) {
        playerToKillAtNight = playerIndex;
        Arrays.stream(players)
                .filter(GamePlayer::isAlive)
                .filter(p -> p.getTeam() == Team.MAFIA)
                .forEach(p -> p.getChannel().writeAndFlush(new InformationMessagePacket(
                        "Вы убили " + players[playerIndex].getVisibleName())));
    }

    public void handleChatMessage(GamePlayer player, String message) {
        GameStage stage = stages.getCurrentStage();

        if (!player.isAlive()) {
            ChatMessagePacket outPacket = new ChatMessagePacket(player.getNumber(), message, ChatMessagePacket.ChatChannel.DEAD);

            Arrays.stream(players)
                    .filter(p -> !p.isAlive())
                    .map(GamePlayer::getChannel)
                    .forEach(ch -> ch.writeAndFlush(outPacket));
        } else if((stage instanceof MafiaDiscussionStage || stage instanceof MafiaVoteStage) &&
                player.getTeam() == Team.MAFIA) {
            ChatMessagePacket outPacket = new ChatMessagePacket(player.getNumber(), message, ChatMessagePacket.ChatChannel.MAFIA);

            Arrays.stream(players)
                    .filter(p -> p.getTeam() == Team.MAFIA) // отправлять ли мертвым мирным?
                    .map(GamePlayer::getChannel)
                    .forEach(ch -> ch.writeAndFlush(outPacket));
        } else {
            ChatMessagePacket outPacket = new ChatMessagePacket(player.getNumber(), message, ChatMessagePacket.ChatChannel.GLOBAL);
            allPlayersChannelGroup.writeAndFlush(outPacket);
        }
    }

    private boolean checkForInnocentsWin() {
        return Arrays.stream(players)
                .filter(GamePlayer::isAlive)
                .noneMatch(p -> p.getTeam() == Team.MAFIA);
    }

    private boolean checkForMafiaWin() {
        int mafiaCount = 0;
        int otherCount = 0;

        for (GamePlayer player : players) {
            if (player.isAlive()) {
                if (player.getTeam() == Team.MAFIA) {
                    mafiaCount++;
                } else {
                    otherCount++;
                }
            }
        }

        return mafiaCount > otherCount;
    }

    private List<Integer> findMostVoted(int[] votesForPlayers) {
        int maxVotes = 0;
        List<Integer> mostVoted = new ArrayList<>();

        for (int player = 0; player < votesForPlayers.length; player++) {
            int votes = votesForPlayers[player];
            if (votes > maxVotes) {
                maxVotes = votes;
                mostVoted.clear();
                mostVoted.add(player);
            } else if (votes == maxVotes) {
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
            if (players[i].isAlive()) {
                return i;
            }
        }

        return 0;
    }

    private List<Integer> getAlivePlayerIndexes() {
        return Arrays.stream(players)
                .filter(GamePlayer::isAlive)
                .map(GamePlayer::getNumber)
                .collect(Collectors.toList());
    }

    public GamePlayer getPlayerByName(String name) {
        return playersByNames.get(name);
    }

}
