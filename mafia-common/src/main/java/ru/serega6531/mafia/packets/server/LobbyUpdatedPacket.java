package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.packets.MafiaPacket;

/**
 * Отправляется всем игрокам при изменении количества игроков в любом лобби
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LobbyUpdatedPacket extends MafiaPacket {

    private String player;
    private boolean joined;
    private GameLobby lobby;

}
