package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerVotePacket extends ClientSidePacket {

    private final int playerIndex;

    public PlayerVotePacket(AuthData authData, int playerIndex) {
        super(authData);
        this.playerIndex = playerIndex;
    }
}
