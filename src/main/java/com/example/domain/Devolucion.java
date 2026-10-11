package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name="devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idVenta;
    private Long idTurno;
    private LocalDateTime fecha;
    private String nombreCliente;
    private String nitCliente;
    private String motivo;
    private BigDecimal totalDevuelto;
    
    private String tipo;
    private Long idVendedor;
    
}
