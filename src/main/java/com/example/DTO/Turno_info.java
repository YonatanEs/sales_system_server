package com.example.DTO;

import java.time.LocalDateTime;

public class Turno_info {

    private int id;
    private String nombreCaja;
    private LocalDateTime fechaApertura;
    private String userApertura;

    public Turno_info(int id, String nombreCaja, LocalDateTime fechaApertura, String userApertura) {
        this.id = id;
        this.nombreCaja = nombreCaja;
        this.fechaApertura = fechaApertura;
        this.userApertura = userApertura;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCaja() {
        return nombreCaja;
    }

    public void setNombreCaja(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getUserApertura() {
        return userApertura;
    }

    public void setUserApertura(String userApertura) {
        this.userApertura = userApertura;
    }
    
}
