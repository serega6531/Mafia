package ru.serega6531.mafia.packets.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.packets.MafiaPacket;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
class ClientSidePacket extends MafiaPacket {

    private String name;

}
