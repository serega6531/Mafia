package ru.serega6531.mafia.enums;

public enum Role {

    UNKNOWN("Неизвестно", Team.INNOCENTS),  // используется только на клиенте
    CITIZEN("Мирный", Team.INNOCENTS),
    MAFIA("Мафия", Team.MAFIA);

    private String roleName;
    private Team team;

    Role(String roleName, Team team) {
        this.roleName = roleName;
        this.team = team;
    }

    public String getRoleName() {
        return roleName;
    }

    public Team getTeam() {
        return team;
    }
}
