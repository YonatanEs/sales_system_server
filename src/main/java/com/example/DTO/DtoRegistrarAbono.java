package com.example.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DtoRegistrarAbono {

    private Long idCredito;
    private Long idTurno;
    private Long idVendedor;
    private BigDecimal montoAbonado;
    private String formaPago;
    private String noDeposito;

}
