package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;

@EqualsAndHashCode(callSuper = true)
@Data
public class JoinLobbyPacket extends ClientSidePacket {

    private int lobbyId;

    public JoinLobbyPacket(AuthData authData, int lobbyId) {
        super(authData);
        this.lobbyId = lobbyId;
    }
}
