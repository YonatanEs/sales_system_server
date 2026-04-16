package com.example.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoInactivar {
    private Long id;
    private boolean inactivar;

    public DtoInactivar(Long id, boolean inactivar) {
        this.id = id;
        this.inactivar = inactivar;
    }
}
