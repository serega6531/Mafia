package ru.serega6531.mafia.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.enums.Team;

@Data
@AllArgsConstructor
public class GamePlayer {

    private String name;
    private String visibleName;
    private Role role;
    private Team team;

}
