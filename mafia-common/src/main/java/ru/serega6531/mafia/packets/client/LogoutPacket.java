package ru.serega6531.mafia.packets.client;

import ru.serega6531.mafia.AuthData;

public class LogoutPacket extends ClientSidePacket {
    public LogoutPacket(AuthData authData) {
        super(authData);
    }
}
