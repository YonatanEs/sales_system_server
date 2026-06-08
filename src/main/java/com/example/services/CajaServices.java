package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCaja;
import com.example.DTO.ModificarCliente;
import com.example.DTO.RegistrarCaja;
import com.example.DTO.RegistrarCliente;
import com.example.Repository.CajaRepository;
import com.example.domain.Caja;
import com.example.domain.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CajaServices {
    
    private CajaRepository cajaRepository;
    
    public CajaServices(CajaRepository cajaRepository){
        this.cajaRepository=cajaRepository;
    }
    
    public List<Caja> listarCajas(String valor) {
        if (valor.isEmpty()) {
            return cajaRepository.findAllByOrderByEstadoAsc();
        } else {
            return cajaRepository.findByNombreContainingIgnoreCaseOrderByEstado(valor);
        }
    }
    
    public DtoResponse registrarCaja(RegistrarCaja request) {
        if (cajaRepository.existsByNombre(request.getNombre())) {
            return new DtoResponse(false, "¡ya existe una caja con el mismo nombre!");
        }

        Caja caja = new Caja();
        caja.setNombre(request.getNombre());
        caja.setEstado("Activo");
        cajaRepository.save(caja);

        return new DtoResponse(true, "¡La caja ha sido registradas correctamente!");
    }
    
    public DtoResponse modificarCaja(ModificarCaja request) {
        if (cajaRepository.existsByNombreAndIdNot(request.getNombre(), request.getId())) {
            return new DtoResponse(false, "¡ya existe una caja con el mismo nombre!");
        }
        
        Optional<Caja> cajaOpt = cajaRepository.findById(request.getId());
        if (cajaOpt.isEmpty()) {
            return new DtoResponse(false, "Seleccione una fila");
        }

        Caja caja = cajaOpt.get();
        caja.setNombre(request.getNombre());
        cajaRepository.save(caja);

        return new DtoResponse(true, "¡La cajas ha sido modificada correctamente!");
    }
    
    public Caja cajaSeleccionada(Long id){
        return cajaRepository.findById(id).orElse(null);
    }
    
    public DtoResponse inactivarCaja(Long id) {
        Optional <Caja> cajaOpt = cajaRepository.findById(id);
        
        if(cajaOpt.isEmpty()){
            return new DtoResponse(false, "Selecciona una fila");
        }
        Caja caja = cajaOpt.get();
        
        if(caja.getEstado().equals("Activo")){
            caja.setEstado("Inactivo");
            cajaRepository.save(caja);
            return new DtoResponse(true, "¡Caja Inactivada correctamente!");
        }else{
            caja.setEstado("Activo");
            cajaRepository.save(caja);
            return new DtoResponse(true, "¡Caja Activada correctamente!");
        }
    }
    
    public List<String> listarSugerencias(){
        return cajaRepository.listarSugerenciasNombres();
    }
    
    
    
}
