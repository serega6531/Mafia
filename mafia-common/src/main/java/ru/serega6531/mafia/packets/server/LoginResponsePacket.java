package ru.serega6531.mafia.packets.server;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.packets.MafiaPacket;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginResponsePacket extends MafiaPacket {

    private byte[] handshakeResponse;
}
