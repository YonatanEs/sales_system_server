package com.example.DTO;

import com.example.domain.DetallesVenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoRecibo {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String nitCliente;
    private String nombreCliente;
    private Usuario_tab vendedor;
    private String metodoPago;
    private String estado;
    private List<DetallesVenta> detallesVenta;
    
}
