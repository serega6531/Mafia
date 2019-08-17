package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.SessionInitialParameters;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateLobbyPacket extends ClientSidePacket {

    private SessionInitialParameters parameters;

    public CreateLobbyPacket(String name, byte[] handshake, SessionInitialParameters parameters) {
        super(name, handshake);
        this.parameters = parameters;
    }
}
