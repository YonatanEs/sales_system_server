package com.example.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate fechaSalida;
    private BigDecimal stock;
    private String concepto;
    
}
