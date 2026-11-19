// Paquete donde vive la clase
package com.example.demo.escalatecnica;

import com.example.demo.aeropuerto.Aeropuerto;             // Entidad relacionada
import com.example.demo.aeropuerto.AeropuertoRepository;   // Para buscar aeropuertos
import com.example.demo.vuelo.Vuelo;                       // Entidad relacionada
import com.example.demo.vuelo.VueloRepository;             // Para buscar vuelos
import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de escalas

@RestController                            // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/escalas-tecnicas")   // Prefijo comun de todas las rutas
@RequiredArgsConstructor                   // Spring inyecta los repositorios por constructor
public class EscalaTecnicaController {

    private final EscalaTecnicaRepository repo;          // Acceso a "escalas_tecnicas"
    private final VueloRepository vueloRepo;             // Acceso a "vuelos"
    private final AeropuertoRepository aeropuertoRepo;   // Acceso a "aeropuertos"

    // ===================== READ: listar todas =====================
    @GetMapping("/listar")            // GET /api/escalas-tecnicas/listar
    public List<EscalaTecnica> listar() {
        return repo.findAll();
    }

    // ===================== READ: listar solo activas =====================
    @GetMapping("/activas")                  // GET /api/escalas-tecnicas/activas
    public List<EscalaTecnica> listarActivas() {
        return repo.findByActivoTrue();
    }

    // ===================== READ: listar por vuelo (ordenadas) =====================
    @GetMapping("/por-vuelo/{vueloId}")                                // GET /api/escalas-tecnicas/por-vuelo/1
    public List<EscalaTecnica> porVuelo(@PathVariable Long vueloId) {
        return repo.findByVueloIdOrderByNumeroOrdenAsc(vueloId);
    }

    // ===================== READ: listar por aeropuerto =====================
    @GetMapping("/por-aeropuerto/{aeropuertoId}")                      // GET /api/escalas-tecnicas/por-aeropuerto/1
    public List<EscalaTecnica> porAeropuerto(@PathVariable Long aeropuertoId) {
        return repo.findByAeropuertoId(aeropuertoId);
    }

    // ===================== READ: obtener una =====================
    @GetMapping("/obtener/{id}")                                          // GET /api/escalas-tecnicas/obtener/1
    public ResponseEntity<EscalaTecnica> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                                   // POST /api/escalas-tecnicas/crear
    public ResponseEntity<?> crear(@Valid @RequestBody EscalaTecnica escala) {
        if (repo.existsByCodigoEscala(escala.getCodigoEscala())) {           // codigoEscala es UNIQUE
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una escala con el codigo: " + escala.getCodigoEscala());
        }
        // === Validar las 2 relaciones (vuelo + aeropuerto) ===
        if (escala.getVuelo() == null || escala.getVuelo().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el vuelo con su id");
        }
        if (escala.getAeropuerto() == null || escala.getAeropuerto().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el aeropuerto de la escala con su id");
        }
        Vuelo vuelo = vueloRepo.findById(escala.getVuelo().getId()).orElse(null);
        if (vuelo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el vuelo con id: " + escala.getVuelo().getId());
        }
        Aeropuerto aero = aeropuertoRepo.findById(escala.getAeropuerto().getId()).orElse(null);
        if (aero == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el aeropuerto con id: " + escala.getAeropuerto().getId());
        }
        // Regla de negocio: no puede repetirse el numero de orden dentro del mismo vuelo
        if (repo.existsByVueloIdAndNumeroOrden(vuelo.getId(), escala.getNumeroOrden())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El vuelo ya tiene una escala con el numero de orden: " + escala.getNumeroOrden());
        }
        escala.setId(null);                                                  // El id lo genera la BD
        escala.setVuelo(vuelo);                                              // Reemplazamos objetos parciales por reales
        escala.setAeropuerto(aero);
        if (escala.getActivo() == null) {
            escala.setActivo(true);
        }
        EscalaTecnica guardada = repo.save(escala);                          // INSERT INTO escalas_tecnicas ...
        return ResponseEntity
                .created(URI.create("/api/escalas-tecnicas/obtener/" + guardada.getId()))
                .body(guardada);
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                           // PUT /api/escalas-tecnicas/actualizar/1
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody EscalaTecnica datos) {
        EscalaTecnica existente = repo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        // Validamos y resolvemos vuelo si viene
        if (datos.getVuelo() != null && datos.getVuelo().getId() != null) {
            Vuelo vuelo = vueloRepo.findById(datos.getVuelo().getId()).orElse(null);
            if (vuelo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el vuelo con id: " + datos.getVuelo().getId());
            }
            existente.setVuelo(vuelo);
        }
        // Validamos y resolvemos aeropuerto si viene
        if (datos.getAeropuerto() != null && datos.getAeropuerto().getId() != null) {
            Aeropuerto aero = aeropuertoRepo.findById(datos.getAeropuerto().getId()).orElse(null);
            if (aero == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el aeropuerto con id: " + datos.getAeropuerto().getId());
            }
            existente.setAeropuerto(aero);
        }
        // Regla de negocio: no puede repetirse el numero de orden dentro del mismo vuelo
        // (solo si cambia el vuelo o el orden)
        Integer nuevoOrden = datos.getNumeroOrden();
        boolean cambioVuelo = !existente.getVuelo().getId().equals(
                datos.getVuelo() != null && datos.getVuelo().getId() != null
                        ? datos.getVuelo().getId() : existente.getVuelo().getId());
        boolean cambioOrden = !existente.getNumeroOrden().equals(nuevoOrden);
        if ((cambioVuelo || cambioOrden)
                && repo.existsByVueloIdAndNumeroOrden(existente.getVuelo().getId(), nuevoOrden)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El vuelo ya tiene una escala con el numero de orden: " + nuevoOrden);
        }
        existente.setCodigoEscala(datos.getCodigoEscala());
        existente.setNumeroOrden(nuevoOrden);
        existente.setCargas(datos.getCargas());
        existente.setDescargas(datos.getDescargas());
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return ResponseEntity.ok(repo.save(existente));                       // UPDATE -> 200 OK
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/escalas-tecnicas/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);                                          // DELETE FROM escalas_tecnicas WHERE id = ?
        return ResponseEntity.noContent().build();
    }
}