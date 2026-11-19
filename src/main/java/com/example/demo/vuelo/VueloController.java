// Paquete donde vive la clase
package com.example.demo.vuelo;

import com.example.demo.avion.Avion;                       // Entidad relacionada
import com.example.demo.avion.AvionRepository;             // Para buscar aviones
import com.example.demo.programavuelo.ProgramaVuelo;       // Entidad relacionada
import com.example.demo.programavuelo.ProgramaVueloRepository; // Para buscar programas
import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.time.LocalDate; // Para la busqueda por rango de fechas
import java.util.List; // Lista de vuelos

@RestController                  // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/vuelos")   // Prefijo comun de todas las rutas
@RequiredArgsConstructor         // Spring inyecta los repositorios por constructor
public class VueloController {

    private final VueloRepository repo;                       // Acceso a "vuelos"
    private final AvionRepository avionRepo;                  // Acceso a "aviones"
    private final ProgramaVueloRepository programaRepo;       // Acceso a "programa_vuelos"

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/vuelos/listar
    public List<Vuelo> listar() {
        return repo.findAll();
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                  // GET /api/vuelos/activos
    public List<Vuelo> listarActivos() {
        return repo.findByActivoTrue();
    }

    // ===================== READ: listar por avion =====================
    @GetMapping("/por-avion/{avionId}")                              // GET /api/vuelos/por-avion/1
    public List<Vuelo> porAvion(@PathVariable Long avionId) {
        return repo.findByAvionId(avionId);
    }

    // ===================== READ: listar por programa =====================
    @GetMapping("/por-programa/{programaId}")                        // GET /api/vuelos/por-programa/1
    public List<Vuelo> porPrograma(@PathVariable Long programaId) {
        return repo.findByProgramaVueloId(programaId);
    }

    // ===================== READ: listar por fecha exacta =====================
    @GetMapping("/por-fecha/{fecha}")                                // GET /api/vuelos/por-fecha/2026-01-15
    public List<Vuelo> porFecha(@PathVariable String fecha) {
        return repo.findByFechaVuelo(LocalDate.parse(fecha));
    }

    // ===================== READ: listar por rango de fechas =====================
    @GetMapping("/por-rango")                                        // GET /api/vuelos/por-rango?desde=2026-01-01&hasta=2026-01-31
    public List<Vuelo> porRango(@RequestParam String desde, @RequestParam String hasta) {
        return repo.findByFechaVueloBetween(LocalDate.parse(desde), LocalDate.parse(hasta));
    }

    // ===================== READ: obtener uno =====================
    @GetMapping("/obtener/{id}")                                     // GET /api/vuelos/obtener/1
    public ResponseEntity<Vuelo> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                              // POST /api/vuelos/crear
    public ResponseEntity<?> crear(@Valid @RequestBody Vuelo vuelo) {
        if (repo.existsByCodigoVuelo(vuelo.getCodigoVuelo())) {        // codigoVuelo es UNIQUE
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un vuelo con el codigo: " + vuelo.getCodigoVuelo());
        }
        // === Validar las 2 relaciones (avion + programa) ===
        if (vuelo.getAvion() == null || vuelo.getAvion().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el avion con su id");
        }
        if (vuelo.getProgramaVuelo() == null || vuelo.getProgramaVuelo().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el programa de vuelo con su id");
        }
        Avion avion = avionRepo.findById(vuelo.getAvion().getId()).orElse(null);
        if (avion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el avion con id: " + vuelo.getAvion().getId());
        }
        ProgramaVuelo programa = programaRepo.findById(vuelo.getProgramaVuelo().getId()).orElse(null);
        if (programa == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el programa de vuelo con id: " + vuelo.getProgramaVuelo().getId());
        }
        vuelo.setId(null);                                             // El id lo genera la BD
        vuelo.setAvion(avion);                                         // Reemplazamos objetos parciales por reales
        vuelo.setProgramaVuelo(programa);
        if (vuelo.getActivo() == null) {
            vuelo.setActivo(true);
        }
        Vuelo guardado = repo.save(vuelo);                             // INSERT INTO vuelos ...
        return ResponseEntity
                .created(URI.create("/api/vuelos/obtener/" + guardado.getId()))
                .body(guardado);
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                          // PUT /api/vuelos/actualizar/1
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody Vuelo datos) {
        Vuelo existente = repo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        // Validamos y resolvemos avion si viene
        if (datos.getAvion() != null && datos.getAvion().getId() != null) {
            Avion avion = avionRepo.findById(datos.getAvion().getId()).orElse(null);
            if (avion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el avion con id: " + datos.getAvion().getId());
            }
            existente.setAvion(avion);
        }
        // Validamos y resolvemos programa si viene
        if (datos.getProgramaVuelo() != null && datos.getProgramaVuelo().getId() != null) {
            ProgramaVuelo programa = programaRepo.findById(datos.getProgramaVuelo().getId()).orElse(null);
            if (programa == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el programa de vuelo con id: " + datos.getProgramaVuelo().getId());
            }
            existente.setProgramaVuelo(programa);
        }
        existente.setCodigoVuelo(datos.getCodigoVuelo());
        existente.setFechaVuelo(datos.getFechaVuelo());
        existente.setPlazasVacias(datos.getPlazasVacias());
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return ResponseEntity.ok(repo.save(existente));                      // UPDATE -> 200 OK
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/vuelos/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // OJO: si este vuelo tiene escalas tecnicas asociadas, la FK impedira el borrado.
        repo.deleteById(id);                                          // DELETE FROM vuelos WHERE id = ?
        return ResponseEntity.noContent().build();
    }
}