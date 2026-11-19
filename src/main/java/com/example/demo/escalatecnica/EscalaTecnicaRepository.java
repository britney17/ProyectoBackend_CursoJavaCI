// Paquete donde vive la interfaz
package com.example.demo.escalatecnica;

import org.springframework.data.jpa.repository.JpaRepository; // CRUD listo
import java.util.List;

public interface EscalaTecnicaRepository extends JpaRepository<EscalaTecnica, Long> {

    // SELECT * FROM escalas_tecnicas WHERE activo = true
    List<EscalaTecnica> findByActivoTrue();

    // SELECT * FROM escalas_tecnicas WHERE vuelo_id = ? ORDER BY numero_orden
    List<EscalaTecnica> findByVueloIdOrderByNumeroOrdenAsc(Long vueloId);

    // SELECT * FROM escalas_tecnicas WHERE aeropuerto_id = ?
    List<EscalaTecnica> findByAeropuertoId(Long aeropuertoId);

    // Para el UNIQUE de codigoEscala en el POST
    boolean existsByCodigoEscala(String codigoEscala);

    // Para el UNIQUE compuesto (vuelo_id, numero_orden) en el POST
    boolean existsByVueloIdAndNumeroOrden(Long vueloId, Integer numeroOrden);
}