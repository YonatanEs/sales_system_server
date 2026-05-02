package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RegistrarProducto {
    private String codigo;
    private String descripcion;
    private double precio_compra;
    private double precio_venta;
    private Long id_proveedor;
    private Long id_medida;
    private Long id_categoria;
}
