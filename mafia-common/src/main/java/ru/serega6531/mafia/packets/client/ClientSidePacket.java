package ru.serega6531.mafia.packets.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.packets.MafiaPacket;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientSidePacket extends MafiaPacket {

    private AuthData authData;

}
