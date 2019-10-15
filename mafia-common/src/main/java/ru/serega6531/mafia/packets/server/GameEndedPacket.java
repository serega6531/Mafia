package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.serega6531.mafia.RoleInfo;
import ru.serega6531.mafia.packets.MafiaPacket;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class GameEndedPacket extends MafiaPacket {

    private List<RoleInfo> allRoles;
    private Reason reason;

    @AllArgsConstructor
    @Getter
    public enum Reason {
        CITIZENS_WON("ПОБЕДА МИРНЫХ"), MAFIA_WON("ПОБЕДА МАФИИ");

        private String text;
    }
}
