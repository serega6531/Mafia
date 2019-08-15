package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JoinLobbyPacket extends ClientSidePacket {

    private int lobbyId;

    public JoinLobbyPacket(String name, int lobbyId) {
        super(name);
        this.lobbyId = lobbyId;
    }
}
