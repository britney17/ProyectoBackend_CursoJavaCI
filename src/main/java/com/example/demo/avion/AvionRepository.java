// Paquete donde vive la interfaz
package com.example.demo.avion;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo
import java.util.List;

public interface AvionRepository extends JpaRepository<Avion, Long> {

    // SELECT * FROM aviones WHERE activo = true
    List<Avion> findByActivoTrue();

    // SELECT * FROM aviones WHERE modelo_avion_id = ?
    List<Avion> findByModeloAvionId(Long modeloAvionId);

    // Para el UNIQUE de numeroSerie en el POST
    boolean existsByNumeroSerie(String numeroSerie);

    Avion findByNumeroSerie(String numeroSerie);

}