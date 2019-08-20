package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientChatMessagePacket extends ClientSidePacket {

    private String message;

    public ClientChatMessagePacket(AuthData authData, String message) {
        super(authData);
        this.message = message;
    }
}
