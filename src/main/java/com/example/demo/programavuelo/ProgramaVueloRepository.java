// Paquete donde vive la interfaz
package com.example.demo.programavuelo;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo

import com.example.demo.vuelo.Vuelo;

import java.util.List;

public interface ProgramaVueloRepository extends JpaRepository<ProgramaVuelo, Long> {

    // SELECT * FROM programa_vuelos WHERE activo = true
    List<ProgramaVuelo> findByActivoTrue();

    // SELECT * FROM programa_vuelos WHERE linea_aerea_id = ?
    List<ProgramaVuelo> findByLineaAereaId(Long lineaAereaId);

    // SELECT * FROM programa_vuelos WHERE aeropuerto_origen_id = ?
    List<ProgramaVuelo> findByAeropuertoOrigenId(Long aeropuertoOrigenId);

    // SELECT * FROM programa_vuelos WHERE aeropuerto_destino_id = ?
    List<ProgramaVuelo> findByAeropuertoDestinoId(Long aeropuertoDestinoId);

    // Para el UNIQUE de codigoPro en el POST
    boolean existsByCodigoPro(String codigoPro);

    // En ProgramaVueloRepository.java
    ProgramaVuelo findByCodigoPro(String codigoPro);

    
}