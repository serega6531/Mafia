package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.packets.MafiaPacket;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ChatMessagePacket extends MafiaPacket {

    private int playerNum;
    private String message;
    private ChatChannel chatChannel;  //TODO use channel

    public enum ChatChannel {
        GLOBAL, MAFIA, DEAD
    }

}
