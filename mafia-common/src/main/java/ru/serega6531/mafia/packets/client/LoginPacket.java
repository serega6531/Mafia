package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.serega6531.mafia.AuthData;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class LoginPacket extends ClientSidePacket {

    public LoginPacket(AuthData authData) {
        super(authData);
    }
}
