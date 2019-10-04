package ru.serega6531.mafia.packets.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.stages.GameStage;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class StageChangePacket extends MafiaPacket {

    private GameStage newStage;

}
