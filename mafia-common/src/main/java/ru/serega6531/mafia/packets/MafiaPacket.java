package ru.serega6531.mafia.packets;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MafiaPacket implements Serializable {

    private String name;

}
