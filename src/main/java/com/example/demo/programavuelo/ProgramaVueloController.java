// Paquete donde vive la clase
package com.example.demo.programavuelo;

import com.example.demo.aeropuerto.Aeropuerto;             // Entidad relacionada
import com.example.demo.aeropuerto.AeropuertoRepository;   // Para buscar aeropuertos
import com.example.demo.linea_aerea.LineaAerea;             // Entidad relacionada
import com.example.demo.linea_aerea.LineaAereaRepository;   // Para buscar la linea aerea
import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de programas

@RestController                          // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/programasvuelo")  // Prefijo comun de todas las rutas
@RequiredArgsConstructor                 // Spring inyecta los repositorios por constructor
public class ProgramaVueloController {

    private final ProgramaVueloRepository repo;          // Acceso a "programa_vuelos"
    private final LineaAereaRepository lineaRepo;        // Acceso a "lineas_aereas"
    private final AeropuertoRepository aeropuertoRepo;   // Acceso a "aeropuertos"

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/programas-vuelo/listar
    public List<ProgramaVuelo> listar() {
        return repo.findAll();
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                  // GET /api/programas-vuelo/activos
    public List<ProgramaVuelo> listarActivos() {
        return repo.findByActivoTrue();
    }

    // ===================== READ: listar por linea aerea =====================
    @GetMapping("/por-linea/{lineaId}")                                // GET /api/programas-vuelo/por-linea/1
    public List<ProgramaVuelo> porLinea(@PathVariable Long lineaId) {
        return repo.findByLineaAereaId(lineaId);
    }

    // ===================== READ: listar por aeropuerto origen =====================
    @GetMapping("/por-origen/{aeropuertoId}")                          // GET /api/programas-vuelo/por-origen/1
    public List<ProgramaVuelo> porOrigen(@PathVariable Long aeropuertoId) {
        return repo.findByAeropuertoOrigenId(aeropuertoId);
    }

    // ===================== READ: listar por aeropuerto destino =====================
    @GetMapping("/por-destino/{aeropuertoId}")                         // GET /api/programas-vuelo/por-destino/1
    public List<ProgramaVuelo> porDestino(@PathVariable Long aeropuertoId) {
        return repo.findByAeropuertoDestinoId(aeropuertoId);
    }

    // ===================== READ: obtener uno =====================
    @GetMapping("/obtener/{id}")                                          // GET /api/programas-vuelo/obtener/1
    public ResponseEntity<ProgramaVuelo> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                                  // POST /api/programas-vuelo/crear
    public ResponseEntity<?> crear(@Valid @RequestBody ProgramaVuelo prog) {
        if (repo.existsByCodigoPro(prog.getCodigoPro())) {                  // codigoPro es UNIQUE
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un programa con el codigo: " + prog.getCodigoPro());
        }
        // === Validar las 3 relaciones (linea + origen + destino) ===
        if (prog.getLineaAerea() == null || prog.getLineaAerea().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar la linea aerea con su id");
        }
        if (prog.getAeropuertoOrigen() == null || prog.getAeropuertoOrigen().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el aeropuerto de origen con su id");
        }
        if (prog.getAeropuertoDestino() == null || prog.getAeropuertoDestino().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el aeropuerto de destino con su id");
        }

        LineaAerea linea = lineaRepo.findById(prog.getLineaAerea().getId()).orElse(null);
        if (linea == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe la linea aerea con id: " + prog.getLineaAerea().getId());
        }
        Aeropuerto origen = aeropuertoRepo.findById(prog.getAeropuertoOrigen().getId()).orElse(null);
        if (origen == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el aeropuerto de origen con id: " + prog.getAeropuertoOrigen().getId());
        }
        Aeropuerto destino = aeropuertoRepo.findById(prog.getAeropuertoDestino().getId()).orElse(null);
        if (destino == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el aeropuerto de destino con id: " + prog.getAeropuertoDestino().getId());
        }
        // Regla de negocio: origen y destino no pueden ser el mismo
        if (origen.getId().equals(destino.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El aeropuerto de origen y destino no pueden ser el mismo");
        }

        prog.setId(null);                                                   // El id lo genera la BD
        prog.setLineaAerea(linea);                                          // Reemplazamos objetos parciales por reales
        prog.setAeropuertoOrigen(origen);
        prog.setAeropuertoDestino(destino);
        if (prog.getActivo() == null) {
            prog.setActivo(true);
        }
        ProgramaVuelo guardado = repo.save(prog);                           // INSERT INTO programa_vuelos ...
        return ResponseEntity
                .created(URI.create("/api/programas-vuelo/obtener/" + guardado.getId()))
                .body(guardado);
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                           // PUT /api/programas-vuelo/actualizar/1
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ProgramaVuelo datos) {
        ProgramaVuelo existente = repo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        // Validamos y resolvemos linea aerea si viene
        if (datos.getLineaAerea() != null && datos.getLineaAerea().getId() != null) {
            LineaAerea linea = lineaRepo.findById(datos.getLineaAerea().getId()).orElse(null);
            if (linea == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe la linea aerea con id: " + datos.getLineaAerea().getId());
            }
            existente.setLineaAerea(linea);
        }
        // Validamos y resolvemos origen si viene
        if (datos.getAeropuertoOrigen() != null && datos.getAeropuertoOrigen().getId() != null) {
            Aeropuerto origen = aeropuertoRepo.findById(datos.getAeropuertoOrigen().getId()).orElse(null);
            if (origen == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el aeropuerto de origen con id: " + datos.getAeropuertoOrigen().getId());
            }
            existente.setAeropuertoOrigen(origen);
        }
        // Validamos y resolvemos destino si viene
        if (datos.getAeropuertoDestino() != null && datos.getAeropuertoDestino().getId() != null) {
            Aeropuerto destino = aeropuertoRepo.findById(datos.getAeropuertoDestino().getId()).orElse(null);
            if (destino == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el aeropuerto de destino con id: " + datos.getAeropuertoDestino().getId());
            }
            existente.setAeropuertoDestino(destino);
        }
        // Regla de negocio despues de aplicar cambios
        if (existente.getAeropuertoOrigen().getId().equals(existente.getAeropuertoDestino().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El aeropuerto de origen y destino no pueden ser el mismo");
        }
        existente.setCodigoPro(datos.getCodigoPro());
        existente.setNumeVuelo(datos.getNumeVuelo());
        existente.setDiasSemana(datos.getDiasSemana());
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return ResponseEntity.ok(repo.save(existente));                       // UPDATE -> 200 OK
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/programas-vuelo/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // OJO: si este programa tiene vuelos asociados, la FK impedira el borrado.
        repo.deleteById(id);                                          // DELETE FROM programa_vuelos WHERE id = ?
        return ResponseEntity.noContent().build();
    }
}