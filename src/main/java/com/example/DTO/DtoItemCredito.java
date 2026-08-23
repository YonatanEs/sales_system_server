package com.example.DTO;

import com.example.domain.Credito;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoItemCredito {
    
    
    private Long id;
    private Long idVenta;
    private LocalDate fechaEmision;
    private LocalDate plazoPago;
    private BigDecimal montoTotal;
    private BigDecimal saldoPendiente;
    private String estado; 
    
    public DtoItemCredito(Credito credito){
        this.id = credito.getId();
        this.idVenta = credito.getIdVenta();
        this.fechaEmision = credito.getFechaEmision();
        this.plazoPago = credito.getPlazoPago();
        this.montoTotal = credito.getMontoTotal();
        this.saldoPendiente = credito.getSaldoPendiente();
        this.estado = credito.getEstado();
    }
    
}
