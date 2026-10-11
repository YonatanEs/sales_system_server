package com.example.DTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoDevolucion {

    private Long idDevolucion;
    private Long idVenta;
    
    private String fecha;
    private String cliente;
    private String nit;
    private String motivo;
    
    private BigDecimal totalDevolver;
    
    private List<DtoDetallesDevolucion> detallesDevolucion;
    
}
