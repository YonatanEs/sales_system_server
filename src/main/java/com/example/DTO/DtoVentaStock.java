package com.example.DTO; 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DtoVentaStock {
    
    private Long idProducto;
    private Long idVenta;
    private LocalDateTime fechaSalida;
    private BigDecimal stock;
    private String concepto;
    
}
