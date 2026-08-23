package com.example.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoHistorialVentas {

    private Long id;
    private LocalDateTime fecha;
    private String nitCliente;
    private String nombreCliente;
    private String total;
    private String metodoPago;
    private String estado;

}
