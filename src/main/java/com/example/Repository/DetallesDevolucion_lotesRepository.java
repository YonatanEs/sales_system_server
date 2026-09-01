package com.example.Repository;

import com.example.domain.Detallesdevolucion_lotes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallesDevolucion_lotesRepository extends JpaRepository<Detallesdevolucion_lotes, Object>{

    Detallesdevolucion_lotes findByIdDetallesVentaLote(Long id);
    
}
