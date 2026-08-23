package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idTurno;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String nombreCliente;
    private String nitCliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idVendedor")
    private Usuario vendedor;
    
    private String metodoPago;
    private String estado;
    
    private String noDeposito;
    private String urlComprobante;

}
