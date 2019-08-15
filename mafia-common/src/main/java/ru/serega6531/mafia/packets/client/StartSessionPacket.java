package ru.serega6531.mafia.packets.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StartSessionPacket extends ClientSidePacket {

    public StartSessionPacket(String name) {
        super(name);
    }
}
