package com.example.demo.linea_aerea;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LineaAereaRepository extends JpaRepository<LineaAerea, Long> {

     List<LineaAerea> findByActivoTrue();

    // Para el UNIQUE de codigoLinea en el POST
    boolean existsByCodigoLinea(String codigoLinea);
    
}