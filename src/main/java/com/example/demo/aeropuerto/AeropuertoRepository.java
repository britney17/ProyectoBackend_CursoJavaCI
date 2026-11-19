// Paquete donde vive la interfaz
package com.example.demo.aeropuerto;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo: save, findAll, findById...
import java.util.List;

public interface AeropuertoRepository extends JpaRepository<Aeropuerto, Long> {

    // SELECT * FROM aeropuertos WHERE activo = true
    List<Aeropuerto> findByActivoTrue();

    // Para el UNIQUE de codigoIATA en el POST
    boolean existsByCodigoAeropuerto(String codigoAeropuerto);
}