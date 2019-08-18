package ru.serega6531.mafia;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AuthData implements Serializable {

    private String name;
    private byte[] handshake;

}
