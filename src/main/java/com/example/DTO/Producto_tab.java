package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Producto_tab {
    
    private Long id;
    private String codigo;
    private String descripcion;
    private double stock;
    private double precio_venta;
    private double precio_compra;
    private Long id_categoria;
    private String categoria;
    private Long id_medida;
    private String medida;
    private Long id_proveedor;
    private String proveedor;
    private String estado;

}
