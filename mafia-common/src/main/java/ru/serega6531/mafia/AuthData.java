package ru.serega6531.mafia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString(of = "name")
@AllArgsConstructor
public class AuthData implements Serializable {

    private String name;
    private byte[] handshake;

}
