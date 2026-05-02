package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarProveedor;
import com.example.DTO.RegistrarCliente;
import com.example.DTO.RegistrarProveedor;
import com.example.Repository.ProveedorRepository;
import com.example.domain.Proveedor;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProveedorServices {
    
    private final ProveedorRepository proveedorRepository;
    
    public ProveedorServices(ProveedorRepository proveedorRepository){
        this.proveedorRepository=proveedorRepository;
    }
    
    public DtoResponse registrarProveedor(RegistrarProveedor request){
        
        if(proveedorRepository.existsByNombre(request.getNombre())){
            return new DtoResponse(false, "¡El proveedor ya existe!");
        }
        
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre());
        proveedorRepository.save(proveedor);
        return new DtoResponse(true, "¡Proveedor registrado correctamente!");
    }
    
    public DtoResponse modificarProveedor(ModificarProveedor request){
        
        if(proveedorRepository.existsByNombreAndIdNot(request.getNombre(), request.getId())){
            return new DtoResponse(false, "¡El proveedor ya existe!");
        }
        
        Optional<Proveedor> proveedorOp = proveedorRepository.findById(request.getId());
        Proveedor proveedor = proveedorOp.get();
        proveedor.setNombre(request.getNombre());
        proveedorRepository.save(proveedor);
        
        return new DtoResponse(true, "¡Proveedor modificado correctamente!");
    }
    
    public List<Proveedor> listarProveedores(){
        return proveedorRepository.findAll();
    }
    
    public Proveedor proveedorSelected(Long id){
        return proveedorRepository.findById(id).orElse(null);
    }
    
}
