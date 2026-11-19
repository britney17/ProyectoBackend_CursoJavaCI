// Paquete donde vive la clase
package com.example.demo.aeropuerto;

import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de aeropuertos

@RestController                          // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/aeropuertos")      // Prefijo comun de todas las rutas de este controller
@RequiredArgsConstructor                 // Spring inyecta el repositorio por constructor
public class AeropuertoController {

    private final AeropuertoRepository repo; // Acceso a la tabla "aeropuertos"

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/aeropuertos/listar
    public List<Aeropuerto> listar() {
        return repo.findAll();        // SELECT * FROM aeropuertos
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                 // GET /api/aeropuertos/activos
    public List<Aeropuerto> listarActivos() {
        return repo.findByActivoTrue();     // SELECT * FROM aeropuertos WHERE activo = true
    }

    // ===================== READ: obtener uno =====================
    @GetMapping("/obtener/{id}")                                       // GET /api/aeropuertos/obtener/1
    public ResponseEntity<Aeropuerto> obtener(@PathVariable Long id) { // Toma el {id} de la URL
        return repo.findById(id)                                       // Busca por id (Optional)
                .map(ResponseEntity::ok)                               // Existe -> 200 OK
                .orElse(ResponseEntity.notFound().build());            // No existe -> 404
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                                   // POST /api/aeropuertos/crear
    public ResponseEntity<?> crear(@Valid @RequestBody Aeropuerto aero) {    // <?>: devuelve Aeropuerto o mensaje de error
        if (repo.existsByCodigoAeropuerto(aero.getCodigoAeropuerto())) {                 // El codigo IATA es UNIQUE: revisamos antes de insertar
            return ResponseEntity.status(HttpStatus.CONFLICT)               // 409 Conflict
                    .body("Ya existe un aeropuerto con ese codigo: " + aero.getCodigoAeropuerto());
        }
        aero.setId(null);                                                    // El id lo genera la BD
        if (aero.getActivo() == null) {                                      // Si el JSON envio "activo": null...
            aero.setActivo(true);                                            // ...lo dejamos activo por defecto
        }
        Aeropuerto guardado = repo.save(aero);                               // INSERT INTO aeropuertos ...
        return ResponseEntity
                .created(URI.create("/api/aeropuertos/obtener/" + guardado.getId())) // 201 Created + Location
                .body(guardado);                                                      // Devuelve el aeropuerto creado
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                         // PUT /api/aeropuertos/actualizar/1
    public ResponseEntity<Aeropuerto> actualizar(@PathVariable Long id,     // id de la URL
                                                 @Valid @RequestBody Aeropuerto datos) { // datos nuevos del JSON
        return repo.findById(id)                                            // Buscamos el aeropuerto existente
                .map(a -> {                                                 // Si existe...
                    a.setCodigoAeropuerto(datos.getCodigoAeropuerto());                 //   nuevo codigo IATA
                    a.setNombre(datos.getNombre());                         //   nuevo nombre
                    a.setCiudad(datos.getCiudad());                         //   nueva ciudad
                    a.setPais(datos.getPais());                             //   nuevo pais
                    if (datos.getActivo() != null) {                        //   solo cambiamos "activo" si lo enviaron
                        a.setActivo(datos.getActivo());
                    }
                    return ResponseEntity.ok(repo.save(a));                 //   UPDATE -> 200 OK
                })
                .orElse(ResponseEntity.notFound().build());                 // Si no existe -> 404
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/aeropuertos/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {     // Sin cuerpo en la respuesta
        if (!repo.existsById(id)) {                                   // Si no existe...
            return ResponseEntity.notFound().build();                 // ...404
        }
        repo.deleteById(id);                                          // DELETE FROM aeropuertos WHERE id = ?
        return ResponseEntity.noContent().build();                    // 204 No Content
    }
}