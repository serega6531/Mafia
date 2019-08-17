package ru.serega6531.mafia;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.serega6531.mafia.enums.Role;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RoleInfo implements Serializable {

    private int playerNum;
    private Role role;

}
