package ru.serega6531.mafia.packets.client;

public class LogoutPacket extends ClientSidePacket {
    public LogoutPacket(String name, byte[] handshake) {
        super(name, handshake);
    }
}
