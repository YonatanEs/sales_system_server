package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoResponseDevolucion {
    
    private boolean success;
    private String message;
    private boolean devolucionTotal;
        
}
