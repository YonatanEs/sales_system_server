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
public class DtoRemoveStock {
    
    private Long idProducto;
    private LocalDateTime fechaSalida;
    private BigDecimal stock;
    private String concepto;
    
}
