package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.SessionInitialParameters;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateLobbyPacket extends ClientSidePacket {

    private SessionInitialParameters parameters;

    public CreateLobbyPacket(AuthData authData, SessionInitialParameters parameters) {
        super(authData);
        this.parameters = parameters;
    }
}
