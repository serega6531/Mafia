package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class LoginPacket extends ClientSidePacket {

    public LoginPacket(String name, byte[] handshakeInitial) {
        super(name, handshakeInitial);
    }
}
