package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCategoria;
import com.example.DTO.RegistrarCategoria;
import com.example.Repository.CategoriaRepository;
import com.example.domain.Categoria;
import com.example.domain.Proveedor;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CategoriaServices {
    
    private final CategoriaRepository categoriaRepository;
    
    public CategoriaServices(CategoriaRepository categoriaRepository){
        this.categoriaRepository=categoriaRepository;
    }
    
    public DtoResponse registrarCategoria(RegistrarCategoria request){
        
        if(categoriaRepository.existsByNombre(request.getNombre())){
            return new DtoResponse(false, "¡La categoria ya existe!");
        }
        
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoriaRepository.save(categoria);
        return new DtoResponse(true, "¡Categoria registrada correctamente!");
    }
    
    public DtoResponse modificarCategoria(ModificarCategoria request){
        
        if(categoriaRepository.existsByNombreAndIdNot(request.getNombre(), request.getId())){
            return new DtoResponse(false, "¡La categoria ya existe!");
        }
        
        Optional<Categoria> categoriaOp = categoriaRepository.findById(request.getId());
        Categoria categoria = categoriaOp.get();
        categoria.setNombre(request.getNombre());
        categoriaRepository.save(categoria);
        
        return new DtoResponse(true, "¡Categoria modificada correctamente!");
    }
    
    public List<Categoria> listarCategoria(){
        return categoriaRepository.findAll();
    }
    
    public Categoria categoriaSelected(Long id){
        return categoriaRepository.findById(id).orElse(null);
    }
}
