package ru.serega6531.mafia.server;

import lombok.Data;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.enums.Team;

@Data
public class GamePlayer {

    private String name;
    private String visibleName;
    private Role role;
    private Team team;

}
