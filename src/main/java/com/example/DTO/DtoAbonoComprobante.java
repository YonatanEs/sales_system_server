package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoAbonoComprobante {
    private Long id;
    private String fechaAbono;
    private String horaAbono;
    private String nombreVendedor;
    private String nombreCliente;
    private String nitCliente;
    private String noDocumento;
    private String montoTotalCredito;
    private String montoAbonado;
    private String saldoAnterior;
    private String nuevoSaldo;
    private String observaciones;
    private String estadoCredito;
}
