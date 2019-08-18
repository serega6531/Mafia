package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.enums.LobbyUpdateType;
import ru.serega6531.mafia.packets.MafiaPacket;

/**
 * Отправляется всем игрокам при создании, удалении лобби или изменении количества игроков в любом лобби
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LobbyUpdatedPacket extends MafiaPacket {

    private LobbyUpdateType type;
    private String player;
    private GameLobby lobby;

}
