package ru.serega6531.mafia.server;

import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.enums.Team;
import ru.serega6531.mafia.server.exceptions.IllegalSessionParametersException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SessionsService {

    private final AtomicInteger idCounter = new AtomicInteger();


    private Map<Integer, GameLobby> lobbiesById = new HashMap<>();
    private Map<String, GameLobby> lobbiesByCreator = new HashMap<>();

    private Map<Integer, GameSession> sessionsById = new HashMap<>();
    private Map<String, GameSession> sessionsByCreator = new HashMap<>();

    public GameLobby createLobby(SessionInitialParameters parameters, String creator) throws IllegalSessionParametersException {
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

        return lobby;
    }

    private void validateParameters(SessionInitialParameters parameters) throws IllegalSessionParametersException {
        if(parameters.getPlayersCount() < 5) {
            throw new IllegalSessionParametersException("Игроков не может быть меньше пяти");
        }

        int mafiaCount = countPlayersOfTeam(parameters, Team.MAFIA);
        int innocentsCount = countPlayersOfTeam(parameters, Team.INNOCENTS);

        if(innocentsCount <= mafiaCount) {
            throw new IllegalSessionParametersException("Мирных должно быть больше, чем мафии");
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
