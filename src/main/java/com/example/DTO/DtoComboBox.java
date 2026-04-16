package com.example.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoComboBox {

    private Long id;
    private String nombre;

    public DtoComboBox(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
}
