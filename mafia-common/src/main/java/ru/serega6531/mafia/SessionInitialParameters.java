package ru.serega6531.mafia;

import lombok.Builder;
import lombok.Data;
import ru.serega6531.mafia.enums.Role;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
public class SessionInitialParameters implements Serializable {

    private int playersCount;
    private Map<Role, Integer> rolesCount;

}
