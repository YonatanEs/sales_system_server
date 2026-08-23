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
public class DtoAddStock {
    private Long id;
    private LocalDateTime fechaEntrada;
    private BigDecimal stock;
    private BigDecimal precioCompra;
    private String concepto;
}
