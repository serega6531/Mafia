package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;

@EqualsAndHashCode(callSuper = true)
@Data
public class StartSessionPacket extends ClientSidePacket {

    public StartSessionPacket(AuthData authData) {
        super(authData);
    }
}
