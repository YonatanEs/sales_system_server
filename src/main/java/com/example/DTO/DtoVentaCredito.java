package com.example.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoVentaCredito {

    private String nombreCliente;
    private String nitCliente;
    private List<DtoPedido> listaPedidos;
    private String metodoPago;
    private BigDecimal totalApagar;
    private Long idVendedor;
    private Long idTurno;
    private LocalDate plazoPago;
    
}
