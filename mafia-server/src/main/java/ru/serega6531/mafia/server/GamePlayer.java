package ru.serega6531.mafia.server;

import io.netty.channel.Channel;
import lombok.Data;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.enums.Role;
import ru.serega6531.mafia.enums.Team;

import java.util.List;

@Data
public class GamePlayer {

    private final int number;
    private final String name;
    private final String visibleName;
    private Role role;
    private Team team;
    private final Channel channel;

    private boolean alive = true;
    private List<RoleInfo> knownRoles;

    public GamePlayer(int number, String name, String visibleName, Role role, Team team, Channel channel) {
        this.number = number;
        this.name = name;
        this.visibleName = visibleName;
        this.role = role;
        this.team = team;
        this.channel = channel;
    }
}
