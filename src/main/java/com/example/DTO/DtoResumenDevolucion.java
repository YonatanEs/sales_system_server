package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoResumenDevolucion {
    
    private String totalActualRecibo;
    private String  metodoPago;
    private String totalDevolver;
    private String metodoDevolucion;

}
