// Paquete donde vive la interfaz
package com.example.demo.vuelo;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo
import java.time.LocalDate;
import java.util.List;

public interface VueloRepository extends JpaRepository<Vuelo, Long> {

    // SELECT * FROM vuelos WHERE activo = true
    List<Vuelo> findByActivoTrue();

    // SELECT * FROM vuelos WHERE avion_id = ?
    List<Vuelo> findByAvionId(Long avionId);

    // SELECT * FROM vuelos WHERE programa_vuelo_id = ?
    List<Vuelo> findByProgramaVueloId(Long programaVueloId);

    // SELECT * FROM vuelos WHERE fecha_vuelo = ?
    List<Vuelo> findByFechaVuelo(LocalDate fechaVuelo);

    // SELECT * FROM vuelos WHERE fecha_vuelo BETWEEN ? AND ?
    List<Vuelo> findByFechaVueloBetween(LocalDate desde, LocalDate hasta);

    // Para el UNIQUE de codigoVuelo en el POST
    boolean existsByCodigoVuelo(String codigoVuelo);

    // En VueloRepository.java
    Vuelo findByCodigoVuelo(String codigoVuelo);
}