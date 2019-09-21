package ru.serega6531.mafia.client;

import lombok.Data;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.SessionInitialParameters;

import java.util.List;

@Data
public class LocalSession {

    private final int id;
    private final SessionInitialParameters parameters;
    private final int playerNumber;
    private final List<String> players;
    private final List<RoleInfo> knownRoles;

}
