// Paquete donde vive la clase
package com.example.demo.modeloavion;

import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de modelos

@RestController                          // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/modelos-avion")    // Prefijo comun de todas las rutas
@RequiredArgsConstructor                 // Spring inyecta el repositorio por constructor
public class ModeloAvionController {

    private final ModeloAvionRepository repo; // Acceso a la tabla "modelo_aviones"

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/modelos-avion/listar
    public List<ModeloAvion> listar() {
        return repo.findAll();
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                 // GET /api/modelos-avion/activos
    public List<ModeloAvion> listarActivos() {
        return repo.findByActivoTrue();
    }

    // ===================== READ: obtener uno =====================
    @GetMapping("/obtener/{id}")                                         // GET /api/modelos-avion/obtener/1
    public ResponseEntity<ModeloAvion> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                                    // POST /api/modelos-avion/crear
    public ResponseEntity<?> crear(@Valid @RequestBody ModeloAvion modelo) {
        if (repo.existsByCodigoModelo(modelo.getCodigoModelo())) {            // codigoModelo es UNIQUE
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un modelo con el codigo: " + modelo.getCodigoModelo());
        }
        modelo.setId(null);                                                   // El id lo genera la BD
        if (modelo.getActivo() == null) {
            modelo.setActivo(true);
        }
        ModeloAvion guardado = repo.save(modelo);                             // INSERT INTO modelo_aviones ...
        return ResponseEntity
                .created(URI.create("/api/modelos-avion/obtener/" + guardado.getId()))
                .body(guardado);
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                           // PUT /api/modelos-avion/actualizar/1
    public ResponseEntity<ModeloAvion> actualizar(@PathVariable Long id,
                                                  @Valid @RequestBody ModeloAvion datos) {
        return repo.findById(id)
                .map(m -> {
                    m.setCodigoModelo(datos.getCodigoModelo());
                    m.setNumeroPlazas(datos.getNumeroPlazas());
                    m.setCapacidadCarga(datos.getCapacidadCarga());
                    if (datos.getActivo() != null) {
                        m.setActivo(datos.getActivo());
                    }
                    return ResponseEntity.ok(repo.save(m));                   // UPDATE -> 200 OK
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/modelos-avion/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // OJO: si algun avion usa este modelo, la FK impedira el borrado.
        // Cuando lleguemos a Avion podras decidir que hacer (bloquear o cascada).
        repo.deleteById(id);                                          // DELETE FROM modelo_aviones WHERE id = ?
        return ResponseEntity.noContent().build();
    }
}