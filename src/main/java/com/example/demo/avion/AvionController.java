// Paquete donde vive la clase
package com.example.demo.avion;

import com.example.demo.modeloavion.ModeloAvion;           // Entidad relacionada
import com.example.demo.modeloavion.ModeloAvionRepository; // Para buscar el modelo por id
import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de aviones

@RestController                     // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/aviones")     // Prefijo comun de todas las rutas de este controller
@RequiredArgsConstructor            // Spring inyecta los repositorios por constructor
public class AvionController {

    private final AvionRepository repo;                     // Acceso a la tabla "aviones"
    private final ModeloAvionRepository modeloAvionRepo;    // Acceso a "modelo_aviones" (para la relacion)

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/aviones/listar
    public List<Avion> listar() {
        return repo.findAll();        // SELECT * FROM aviones (JOIN a modelo_aviones por EAGER)
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                 // GET /api/aviones/activos
    public List<Avion> listarActivos() {
        return repo.findByActivoTrue();     // SELECT * FROM aviones WHERE activo = true
    }

    // ===================== READ: listar por modelo =====================
    @GetMapping("/por-modelo/{modeloId}")                              // GET /api/aviones/por-modelo/1
    public List<Avion> porModelo(@PathVariable Long modeloId) {
        return repo.findByModeloAvionId(modeloId);                     // WHERE modelo_avion_id = ?
    }

    // ===================== READ: obtener uno =====================
    @GetMapping("/obtener/{id}")                                    // GET /api/aviones/obtener/1
    public ResponseEntity<Avion> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                             // POST /api/aviones/crear
    public ResponseEntity<?> crear(@Valid @RequestBody Avion avion) {
        if (repo.existsByNumeroSerie(avion.getNumeroSerie())) {        // El numero de serie es UNIQUE
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un avion con el numero de serie: " + avion.getNumeroSerie());
        }
        // La relacion viene con solo el id del modelo -> validamos que exista
        if (avion.getModeloAvion() == null || avion.getModeloAvion().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el modelo del avion con su id");
        }
        ModeloAvion modelo = modeloAvionRepo.findById(avion.getModeloAvion().getId()).orElse(null);
        if (modelo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el modelo de avion con id: " + avion.getModeloAvion().getId());
        }
        avion.setId(null);                                              // El id lo genera la BD
        avion.setModeloAvion(modelo);                                   // Reemplazamos el objeto "parcial" por el real de BD
        if (avion.getActivo() == null) {
            avion.setActivo(true);
        }
        Avion guardado = repo.save(avion);                              // INSERT INTO aviones (..., modelo_avion_id) VALUES (...)
        return ResponseEntity
                .created(URI.create("/api/aviones/obtener/" + guardado.getId()))
                .body(guardado);
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                     // PUT /api/aviones/actualizar/1
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody Avion datos) {
        Avion existente = repo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        // Si envian un modelo, validamos que exista antes de asignarlo
        if (datos.getModeloAvion() != null && datos.getModeloAvion().getId() != null) {
            ModeloAvion modelo = modeloAvionRepo.findById(datos.getModeloAvion().getId()).orElse(null);
            if (modelo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el modelo de avion con id: " + datos.getModeloAvion().getId());
            }
            existente.setModeloAvion(modelo);
        }
        existente.setNumeroSerie(datos.getNumeroSerie());
        existente.setNombre(datos.getNombre());
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return ResponseEntity.ok(repo.save(existente));                 // UPDATE -> 200 OK
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                              // DELETE /api/aviones/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // OJO: si este avion tiene vuelos asociados, la FK impedira el borrado.
        // Mas adelante (cuando exista Vuelo) habra que decidir: borrar en cascada o bloquear.
        repo.deleteById(id);                                      // DELETE FROM aviones WHERE id = ?
        return ResponseEntity.noContent().build();                // 204 No Content
    }
}