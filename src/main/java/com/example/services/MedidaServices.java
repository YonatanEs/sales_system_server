package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarMedida;
import com.example.DTO.RegistrarMedida;
import com.example.Repository.MedidaRepository;
import com.example.domain.Medida;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MedidaServices {
       
    private final MedidaRepository medidaRepository;
    
    public MedidaServices(MedidaRepository medidaRepository){
        this.medidaRepository=medidaRepository;
    }
    
    public DtoResponse registrarMedida(RegistrarMedida request){
        
        if(medidaRepository.existsByNombre(request.getNombre())){
            return new DtoResponse(false, "¡La medida ya existe!");
        }
        if(medidaRepository.existsByAbreviatura(request.getAbreviatura())){
            return new DtoResponse(false, "¡La abreviatura ya existe!");
        }
        
        Medida medida = new Medida();
        medida.setNombre(request.getNombre());
        medida.setAbreviatura(request.getAbreviatura());
        medidaRepository.save(medida);
        
        return new DtoResponse(true, "¡Medida registrada correctamente!");
    }
    
    public DtoResponse modificarMedida(ModificarMedida request){
        
        if(medidaRepository.existsByNombreAndIdNot(request.getNombre(), request.getId())){
            return new DtoResponse(false, "¡La medida ya existe!");
        }
        if(medidaRepository.existsByAbreviaturaAndIdNot(request.getAbreviatura(), request.getId())){
            return new DtoResponse(false, "¡La abreviatura ya existe!");
        }
        
        Optional<Medida> medidaOp = medidaRepository.findById(request.getId());
        Medida medida = medidaOp.get();
        medida.setNombre(request.getNombre());
        medida.setAbreviatura(request.getAbreviatura());
        medidaRepository.save(medida);
        
        return new DtoResponse(true, "¡Medida modificada correctamente!");
    }
    
    public List<Medida> listarMedida(){
        return medidaRepository.findAll();
    }
    
    public Medida medidaSelected(Long id){
        return medidaRepository.findById(id).orElse(null);
    }
}
