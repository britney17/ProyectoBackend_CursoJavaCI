// Paquete donde vive la interfaz
package com.example.demo.modeloavion;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo
import java.util.List;

public interface ModeloAvionRepository extends JpaRepository<ModeloAvion, Long> {

    // SELECT * FROM modelo_aviones WHERE activo = true
    List<ModeloAvion> findByActivoTrue();

    // Para el UNIQUE de codigoModelo en el POST
    boolean existsByCodigoModelo(String codigoModelo);
}