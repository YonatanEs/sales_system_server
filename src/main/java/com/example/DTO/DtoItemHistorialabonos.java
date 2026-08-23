package com.example.DTO;

import com.example.domain.Abono;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DtoItemHistorialabonos {
    private Long idAbono;
    private Long idRecibo;
    private String fecha;
    private String monto;
    private String formaPago;

    public DtoItemHistorialabonos(Abono abono){
        this.idAbono=abono.getId();
        this.idRecibo=abono.getIdVenta();
        this.fecha=abono.getFechaAbono().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a"));
        this.monto= "Q "+abono.getMontoAbonado().setScale(2, RoundingMode.HALF_UP).toPlainString();
        this.formaPago=abono.getFormaPago();
    }
}
