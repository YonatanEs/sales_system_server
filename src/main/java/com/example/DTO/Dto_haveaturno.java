package com.example.DTO;

public class Dto_haveaturno {

    private boolean haveaturno;
    private Turno_info turnoInfo;

    public Dto_haveaturno(boolean haveaturno, Turno_info turnoInfo) {
        this.haveaturno = haveaturno;
        this.turnoInfo = turnoInfo;
    }

    public boolean isHaveaturno() {
        return haveaturno;
    }

    public void setHaveaturno(boolean haveaturno) {
        this.haveaturno = haveaturno;
    }

    public Turno_info getTurnoInfo() {
        return turnoInfo;
    }

    public void setTurnoInfo(Turno_info turnoInfo) {
        this.turnoInfo = turnoInfo;
    }
    
}
