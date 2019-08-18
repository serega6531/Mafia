package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.GameLobby;
import ru.serega6531.mafia.packets.MafiaPacket;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LoginResponsePacket extends MafiaPacket {

    private AuthData authData;
    private Collection<GameLobby> lobbies;

}
