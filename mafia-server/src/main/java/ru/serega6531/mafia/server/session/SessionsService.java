package ru.serega6531.mafia.server.session;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.enums.Team;
import ru.serega6531.mafia.server.GamePlayer;
import ru.serega6531.mafia.server.MafiaServerHandler;
import ru.serega6531.mafia.server.exceptions.MafiaErrorMessageException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SessionsService {

    private final AtomicInteger idCounter = new AtomicInteger();

    private Map<String, GameLobby> lobbiesByPlayer = new HashMap<>();
    private Map<String, GameSession> sessionsByPlayer = new HashMap<>();

    private Map<Integer, GameLobby> lobbiesById = new HashMap<>();
    private Map<String, GameLobby> lobbiesByCreator = new HashMap<>();

    private Map<Integer, GameSession> sessionsById = new HashMap<>();
    private Map<String, GameSession> sessionsByCreator = new HashMap<>();

    private Map<Integer, ChannelGroup> channelGroups = new HashMap<>();

    private final MafiaServerHandler serverHandler;

    public SessionsService(MafiaServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public GameLobby createLobby(SessionInitialParameters parameters, String creator) throws MafiaErrorMessageException {
        validateParameters(parameters);

        final GameLobby lobby = new GameLobby(idCounter.incrementAndGet(), creator, parameters);
        lobby.getPlayers().add(creator);

        System.out.printf("Создана новая сессия с id %d (%d игроков, %s)\n",
                lobby.getId(), parameters.getPlayersCount(),
                parameters.getRolesCount().entrySet()
                        .stream()
                        .map(ent -> ent.getValue() + " " + ent.getKey().getRoleName())
                        .collect(Collectors.joining(", ")));

        lobbiesByPlayer.put(creator, lobby);
        lobbiesById.put(lobby.getId(), lobby);
        lobbiesByCreator.put(creator, lobby);
        channelGroups.put(lobby.getId(), new DefaultChannelGroup("session-" + lobby.getId(), GlobalEventExecutor.INSTANCE));

        return lobby;
    }

    public GameSession startSession(String player) throws MafiaErrorMessageException {
        GameLobby lobby = getLobbyByCreator(player);
        if (lobby == null) {
            throw new MafiaErrorMessageException("Вы не создатель лобби");
        }

        if (lobby.getPlayers().size() != lobby.getParameters().getPlayersCount()) {
            throw new MafiaErrorMessageException("Ещё не набралось " + lobby.getParameters().getPlayersCount() +
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
            GamePlayer gp = new GamePlayer(i + 1, playerName, String.valueOf(i + 1), role, role.getTeam(), serverHandler.getChannelForPlayer(player));
            gamePlayers.add(gp);
        }

        final List<RoleInfo> mafiaRoles = gamePlayers.stream()
                .filter(gp -> gp.getTeam() == Team.MAFIA)
                .map(gp -> new RoleInfo(gp.getNumber(), gp.getRole()))
                .collect(Collectors.toList());

        for (GamePlayer gp : gamePlayers) {
            if (gp.getTeam() == Team.MAFIA) {
                gp.setKnownRoles(mafiaRoles);
            } else {
                gp.setKnownRoles(new ArrayList<>(Collections.singletonList(
                        new RoleInfo(gp.getNumber(), gp.getRole()))));
            }
        }

        GameSession session = new GameSession(lobby.getId(), lobby.getCreator(), lobby.getParameters(), gamePlayers.toArray(new GamePlayer[0]));

        System.out.printf("[%d] Игра началась: %s\n", session.getId(),
                gamePlayers.stream()
                        .map(p -> p.getName() + " " + p.getRole().getRoleName())
                        .collect(Collectors.joining(", ")));

        lobbiesById.remove(lobby.getId());
        lobbiesByCreator.remove(lobby.getCreator());
        sessionsById.put(session.getId(), session);
        sessionsByCreator.put(session.getCreator(), session);

        for (String p : players) {
            lobbiesByPlayer.remove(p);
            sessionsByPlayer.put(p, session);
        }

        return session;
    }

    public GameLobby joinLobby(String player, int id) throws MafiaErrorMessageException {
        GameLobby currentLobby = getLobbyByPlayer(player);
        GameSession currentSession = getSessionByPlayer(player);

        GameLobby lobby = lobbiesById.get(id);
        if (lobby == null) {
            throw new MafiaErrorMessageException("Это лобби перестало существовать");
        }

        if ((currentLobby != null && !currentLobby.equals(lobby)) || currentSession != null) {
            throw new MafiaErrorMessageException("Вы уже состоите в лобби");
        }

        if (lobby.getPlayers().contains(player)) {
            return null;   // ничего не делаем на случай дублирующегося пакета
        }

        lobbiesByPlayer.put(player, lobby);
        lobby.getPlayers().add(player);

        System.out.printf("[%d] Присоединился игрок %s\n", lobby.getId(), player);

        return lobby;
    }

    public List<GameLobby> getAllLobbies() {
        return new ArrayList<>(lobbiesById.values());
    }

    public GameLobby getLobbyByCreator(String creator) {
        return lobbiesByCreator.get(creator);
    }

    public GameLobby getLobbyByPlayer(String player) {
        return lobbiesByPlayer.get(player);
    }

    public GameSession getSessionByPlayer(String player) {
        return sessionsByPlayer.get(player);
    }

    public ChannelGroup getChannelGroup(int sessionId) {
        return channelGroups.get(sessionId);
    }

    private void validateParameters(SessionInitialParameters parameters) throws MafiaErrorMessageException {
        if (parameters.getPlayersCount() < 5) {
            throw new MafiaErrorMessageException("Игроков не может быть меньше пяти");
        }

        int mafiaCount = countPlayersOfTeam(parameters, Team.MAFIA);
        int innocentsCount = countPlayersOfTeam(parameters, Team.INNOCENTS);

        if (innocentsCount <= mafiaCount) {
            throw new MafiaErrorMessageException("Мирных должно быть больше, чем мафии");
        }

        if (mafiaCount + innocentsCount != parameters.getPlayersCount()) {
            throw new MafiaErrorMessageException("Количество ролей не совпадает с количеством игроков");
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
