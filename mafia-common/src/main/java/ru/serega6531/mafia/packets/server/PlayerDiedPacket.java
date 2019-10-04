package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.packets.MafiaPacket;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class PlayerDiedPacket extends MafiaPacket {

    private int playerIndex;

}
