package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginPacket extends ClientSidePacket {

    //TODO implement handshake

    public LoginPacket(String name) {
        super(name);
    }
}
