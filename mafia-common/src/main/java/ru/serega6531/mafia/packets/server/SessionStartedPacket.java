package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.SessionInitialParameters;
import ru.serega6531.mafia.packets.MafiaPacket;

import java.util.List;

/**
 * Отправляется каждому игроку в начавшейся сессии, содержит номер игрока,
 * список всех игроков в нужном порядке и список известных ролей.
 * Обычные игроки знают только себя, мафия знает себя и остальных мафий.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class SessionStartedPacket extends MafiaPacket {

    private final int sessionId;
    private final SessionInitialParameters parameters;
    private final int playerNumber;
    private final List<String> players;
    private final List<RoleInfo> knownRoles;

}
