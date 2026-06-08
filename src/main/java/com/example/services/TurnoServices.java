package com.example.services;

import com.example.DTO.DtoCerrarTurno;
import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoUnirATurno;
import com.example.DTO.Dto_cajaSelected_abierta;
import com.example.DTO.Dto_turnoUserAuth;
import com.example.DTO.RegistrarTurno;
import com.example.DTO.ValorRequestAndId;
import com.example.DTO.ValorRequestPag;
import com.example.Repository.MovimientosTurnoRepository;
import com.example.Repository.TurnoRepository;
import com.example.Repository.UserTurnoRepositoriy;
import com.example.domain.Caja;
import com.example.domain.MovimientosTurno;
import com.example.domain.Turno;
import com.example.domain.UserTurnos;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TurnoServices {

    private TurnoRepository turnoRepository;
    private UserTurnoRepositoriy userTurnoRepository;
    private MovimientosTurnoRepository movimientosTurnoRepository;

    public Page<Turno> listarTurnos(ValorRequestPag val) {
        Pageable pageable = PageRequest.of(val.getPage(), val.getSize());
        Page<Turno> turnosPage;

        if (val.getBusqueda() != null && !val.getBusqueda().trim().isEmpty()) {
            return turnoRepository.buscarPorFecha(val.getBusqueda(), pageable);
        }

        return turnoRepository.findAllCustomOrder(pageable);
    }

    public Dto_turnoUserAuth isTurnoLogged(Long idUser) {
        Optional<Turno> turnoOp = turnoRepository.findByUserMasterIdAndEstado(idUser, "Abierto");
        System.out.println(turnoOp);
        if (turnoOp.isEmpty()) {
            List<Turno> turnosAbiertos = turnoRepository.findByEstado("Abierto");
            if (turnosAbiertos == null || turnosAbiertos.isEmpty()) {
                return new Dto_turnoUserAuth(Long.valueOf(0), false);
            } else {
                Turno tr = null;
                for (Turno t : turnosAbiertos) {
                    if (userTurnoRepository.existsByIdTurnoAndIdUsuario(t.getId(), idUser)) {
                        tr = t;
                        break;
                    }
                }

                if (tr == null) {
                    return new Dto_turnoUserAuth(Long.valueOf(0), false);
                } else {
                    return new Dto_turnoUserAuth(tr.getId(), false);
                }
            }
        } else {
            Turno turno = turnoOp.get();
            return new Dto_turnoUserAuth(turno.getId(), true);
        }
    }

    public Turno turnoSelected(Long idTurno) {
        return turnoRepository.findById(idTurno).orElse(null);
    }

    public Dto_cajaSelected_abierta validarCajaSeleccionada(Caja caja) {

        if (turnoRepository.existsByCajaIdAndEstado(caja.getId(), "Abierto")) {
            Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), "Abierto");

            return new Dto_cajaSelected_abierta(true, "La " + caja.getNombre() + " ya cuenta con un turno abierto en este momento.\n"
                    + "¿Deseas unirte al turno actual para continuar registrando operaciones?", turno.getId());
        } else {
            return new Dto_cajaSelected_abierta(false, "", 0L);
        }
    }

    public DtoResponse abrirTurno(RegistrarTurno request) {
        if (turnoRepository.existsByCajaIdAndEstado(request.getCaja().getId(), "Abierto")) {
            return new DtoResponse(false, "¡La caja ya tiene un turno abierto!");
        }

        Turno turno = new Turno();
        turno.setCaja(request.getCaja());
        turno.setUserMaster(request.getUserMaster());
        turno.setFechaApertura(LocalDateTime.now());
        turno.setSaldoInicial(request.getSaldoInicial());
        turno.setSaldoFinal(request.getSaldoInicial());
        turno = turnoRepository.save(turno);

        MovimientosTurno mov = new MovimientosTurno();
        mov.setIdTurno(turno.getId());
        mov.setFecha(LocalDateTime.now());
        mov.setTipo("Saldo inicial");
        mov.setConcepto("Saldo inicial de apertura");
        mov.setImporte(turno.getSaldoInicial());
        movimientosTurnoRepository.save(mov);

        return new DtoResponse(true, "¡Turno abierto correctamente!");
    }

    public DtoResponse unirATurno(DtoUnirATurno unir) {
        if (userTurnoRepository.existsByIdTurnoAndIdUsuario(unir.getIdTurno(), unir.getIdUsuario())) {
            return new DtoResponse(false, "Ya estas unido a un turno");
        }

        UserTurnos usTurno = new UserTurnos();
        usTurno.setIdTurno(unir.getIdTurno());
        usTurno.setIdUsuario(unir.getIdUsuario());
        usTurno.setFechaUnion(LocalDateTime.now());
        userTurnoRepository.save(usTurno);

        return new DtoResponse(true, "Te has unido al turno");
    }

    public DtoResponseOb<Turno> cerrarTurno(DtoCerrarTurno dtoCerrar, boolean forzado) {
        if (!turnoRepository.existsByIdAndEstado(dtoCerrar.getIdTurno(), "Abierto")) {
            return new DtoResponseOb<>(false, "¡El turno ya ha sido cerrado!", null);
        }

        Turno turno = turnoRepository.findById(dtoCerrar.getIdTurno()).orElse(null);
        turno.setUserCierre(dtoCerrar.getUserCierre());
        turno.setFechaCierre(LocalDateTime.now());

        BigDecimal diferencia = dtoCerrar.getArqueo().subtract(turno.getSaldoFinal());

        BigDecimal saldoFaltante = BigDecimal.ZERO;
        BigDecimal saldoSobrante = BigDecimal.ZERO;

        int comparacion = diferencia.compareTo(BigDecimal.ZERO);

        if (comparacion < 0) {
            saldoFaltante = diferencia.abs();
            saldoSobrante = BigDecimal.ZERO;
        } else if (comparacion > 0) {
            saldoFaltante = BigDecimal.ZERO;
            saldoSobrante = diferencia;
        } else {
            saldoFaltante = BigDecimal.ZERO;
            saldoSobrante = BigDecimal.ZERO;
        }

        turno.setSaldoSobrante(saldoSobrante);
        turno.setSaldoFaltante(saldoFaltante);

        turno.setArqueo(dtoCerrar.getArqueo());
        if (forzado) {
            turno.setEstado("Forzado");
        } else {
            turno.setEstado("Cerrado");
        }

        turno = turnoRepository.save(turno);
        return new DtoResponseOb<>(true, "¡Turno cerrado exitosamente!", turno);
    }

    public DtoResponse ingresoRetiroEfectivo(DtoEfectivoCaja efectivo) {
        Turno t = turnoRepository.findById(efectivo.getIdTurno()).orElse(null);

        if (t == null) {
            return new DtoResponse(false, "El turno especificado no existe");
        }
        if (!t.getEstado().equalsIgnoreCase("Abierto")) {
            return new DtoResponse(false, "¡El turno está cerrado!");
        }

        System.out.println(efectivo.getImporte() + " este es el importe ");

        String message;
        if (efectivo.getTipoMovimiento().equalsIgnoreCase("ingreso")) {
            t.setIngresos(t.getIngresos().add(efectivo.getImporte()));
            message = "¡Ingreso realizado existosamente!";
        } else {
            if (efectivo.getImporte().compareTo(t.getSaldoFinal()) > 0) {
                return new DtoResponse(false, "¡No se puede realizar el retiro! El importe solicitado (Q. "
                        + efectivo.getImporte() + ") supera el saldo actual en caja (Q. " + t.getSaldoFinal() + ").");
            }

            t.setSalidas(t.getSalidas().add(efectivo.getImporte()));
            message = "¡Retiro realizado existosamente!";
        }
        BigDecimal saldoFinal = t.getSaldoInicial()
                .add(t.getIngresos())
                .add(t.getVentas())
                .add(t.getCobroCredito())
                .subtract(t.getSalidas());
        t.setSaldoFinal(saldoFinal);
        turnoRepository.save(t);

        MovimientosTurno mov = new MovimientosTurno();
        mov.setIdTurno(efectivo.getIdTurno());
        mov.setFecha(LocalDateTime.now());
        mov.setTipo(efectivo.getTipoMovimiento());
        mov.setConcepto(efectivo.getConcepto());
        mov.setImporte(efectivo.getImporte());
        movimientosTurnoRepository.save(mov);

        return new DtoResponse(true, message);
    }

    public List<MovimientosTurno> listarMovimientosTurnos(ValorRequestAndId val) {
        List<MovimientosTurno> mov = new ArrayList<>();
        if (val.getValor() != null && !val.getValor().trim().isEmpty()) {
            mov = movimientosTurnoRepository.buscarPorFechaYIdTurno(val.getValor(), val.getId());
        } else {
            mov = movimientosTurnoRepository.findByIdTurnoOrderByFechaAsc(val.getId());
        }
        return mov;
    }
}
