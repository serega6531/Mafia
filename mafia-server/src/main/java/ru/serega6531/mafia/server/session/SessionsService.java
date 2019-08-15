package ru.serega6531.mafia.server.session;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.enums.Team;
import ru.serega6531.mafia.server.GamePlayer;
import ru.serega6531.mafia.server.exceptions.IllegalSessionStateException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SessionsService {

    private final AtomicInteger idCounter = new AtomicInteger();


    private Map<Integer, GameLobby> lobbiesById = new HashMap<>();
    private Map<String, GameLobby> lobbiesByCreator = new HashMap<>();

    private Map<Integer, GameSession> sessionsById = new HashMap<>();
    private Map<String, GameSession> sessionsByCreator = new HashMap<>();

    private Map<Integer, ChannelGroup> channelGroups = new HashMap<>();

    public GameLobby createLobby(SessionInitialParameters parameters, String creator) throws IllegalSessionStateException {
        validateParameters(parameters);

        final GameLobby lobby = new GameLobby(idCounter.incrementAndGet(), creator, parameters);

        System.out.printf("Создана новая сессия с id %d (%d игроков, %s)\n",
                lobby.getId(), parameters.getPlayersCount(),
                parameters.getRolesCount().entrySet()
                        .stream()
                        .map(ent -> ent.getValue() + " " + ent.getKey().getRoleName())
                        .collect(Collectors.joining(", ")));

        lobbiesById.put(lobby.getId(), lobby);
        lobbiesByCreator.put(creator, lobby);
        channelGroups.put(lobby.getId(), new DefaultChannelGroup("session-" + lobby.getId(), GlobalEventExecutor.INSTANCE));

        return lobby;
    }

    public GameSession startSession(GameLobby lobby) throws IllegalSessionStateException {
        if (lobby.getPlayers().size() != lobby.getParameters().getPlayersCount()) {
            throw new IllegalSessionStateException("Ещё не набралось " + lobby.getParameters().getPlayersCount() +
                    " игроков");
        }

        final List<String> players = lobby.getPlayers();
        List<Role> roles = new ArrayList<>();
        lobby.getParameters().getRolesCount().entrySet()
                .stream()
                .map(ent -> Collections.nCopies(ent.getValue(), ent.getKey()))
                .forEach(roles::addAll);

        Collections.shuffle(roles);
        Collections.shuffle(players);

        List<GamePlayer> gamePlayers = new ArrayList<>(players.size());

        for (int i = 0; i < players.size(); i++) {
            String playerName = players.get(i);
            final Role role = roles.get(i);
            GamePlayer gp = new GamePlayer(playerName, "Игрок " + (i + 1), role, role.getTeam());
            gamePlayers.add(gp);
        }

        GameSession session = new GameSession(lobby.getId(), lobby.getCreator(), lobby.getParameters(), gamePlayers.toArray(new GamePlayer[0]));

        System.out.printf("Начата игра с id %d на %d игроков: %s\n", session.getId(), players.size(),
                gamePlayers.stream()
                        .map(p -> p.getName() + " " + p.getRole().getRoleName())
                        .collect(Collectors.joining(", ")));

        lobbiesById.remove(lobby.getId());
        lobbiesByCreator.remove(lobby.getCreator());
        sessionsById.put(session.getId(), session);
        sessionsByCreator.put(session.getCreator(), session);

        return session;
    }

    private void validateParameters(SessionInitialParameters parameters) throws IllegalSessionStateException {
        if (parameters.getPlayersCount() < 5) {
            throw new IllegalSessionStateException("Игроков не может быть меньше пяти");
        }

        int mafiaCount = countPlayersOfTeam(parameters, Team.MAFIA);
        int innocentsCount = countPlayersOfTeam(parameters, Team.INNOCENTS);

        if (innocentsCount <= mafiaCount) {
            throw new IllegalSessionStateException("Мирных должно быть больше, чем мафии");
        }

        if(mafiaCount + innocentsCount != parameters.getPlayersCount()) {
            throw new IllegalSessionStateException("Количество ролей не совпадает с количеством игроков");
        }
    }

    private int countPlayersOfTeam(SessionInitialParameters parameters, Team team) {
        return parameters.getRolesCount().entrySet()
                .stream()
                .filter(ent -> ent.getKey().getTeam() == team)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

}
