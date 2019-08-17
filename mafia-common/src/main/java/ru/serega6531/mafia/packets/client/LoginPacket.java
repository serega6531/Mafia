package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginPacket extends ClientSidePacket {

    public LoginPacket(String name, byte[] handshakeInitial) {
        super(name, handshakeInitial);
    }
}
