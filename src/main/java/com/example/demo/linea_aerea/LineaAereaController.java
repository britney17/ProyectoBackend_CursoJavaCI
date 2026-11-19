// Paquete donde vive la clase
package com.example.demo.linea_aerea;

import jakarta.validation.Valid;                   // Activa las validaciones de la entidad
import lombok.RequiredArgsConstructor;             // Lombok: constructor para los campos "final"
import org.springframework.http.HttpStatus;        // Codigos HTTP con nombre (CONFLICT = 409)
import org.springframework.http.ResponseEntity;    // Respuesta HTTP: codigo de estado + cuerpo
import org.springframework.web.bind.annotation.*;  // @RestController, @GetMapping, @PostMapping...

import java.net.URI;   // Para la cabecera "Location" del recurso creado
import java.util.List; // Lista de lineas aereas

@RestController                     // Atiende peticiones HTTP y responde JSON
@RequestMapping("/api/lineas-aereas") // Prefijo comun de todas las rutas de este controller
@RequiredArgsConstructor            // Spring inyecta el repositorio por constructor
public class LineaAereaController {

    private final LineaAereaRepository repo; // Acceso a la tabla "lineas_aereas"

    // ===================== READ: listar todos =====================
    @GetMapping("/listar")            // GET /api/lineas-aereas/listar
    public List<LineaAerea> listar() {
        return repo.findAll();        // SELECT * FROM lineas_aereas
    }

    // ===================== READ: listar solo activos =====================
    @GetMapping("/activos")                 // GET /api/lineas-aereas/activos
    public List<LineaAerea> listarActivos() {
        return repo.findByActivoTrue();     // SELECT * FROM lineas_aereas WHERE activo = true
    }

    // ===================== READ: obtener una =====================
    @GetMapping("/obtener/{id}")                                        // GET /api/lineas-aereas/obtener/1
    public ResponseEntity<LineaAerea> obtener(@PathVariable Long id) {  // Toma el {id} de la URL
        return repo.findById(id)                                        // Busca por id (Optional)
                .map(ResponseEntity::ok)                                // Existe -> 200 OK
                .orElse(ResponseEntity.notFound().build());             // No existe -> 404
    }

    // ===================== CREATE: insertar =====================
    @PostMapping("/crear")                                                  // POST /api/lineas-aereas/crear
    public ResponseEntity<?> crear(@Valid @RequestBody LineaAerea linea) {  // <?>: devuelve LineaAerea o mensaje de error
        if (repo.existsByCodigoLinea(linea.getCodigoLinea())) {             // El codigo es UNIQUE: revisamos antes de insertar
            return ResponseEntity.status(HttpStatus.CONFLICT)               // 409 Conflict
                    .body("Ya existe una linea aerea con el codigo: " + linea.getCodigoLinea());
        }
        linea.setId(null);                                                  // El id lo genera la BD
        if (linea.getActivo() == null) {                                    // Si el JSON envio "activo": null...
            linea.setActivo(true);                                          // ...lo dejamos activo por defecto
        }
        LineaAerea guardada = repo.save(linea);                             // INSERT INTO lineas_aereas ...
        return ResponseEntity
                .created(URI.create("/api/lineas-aereas/obtener/" + guardada.getId())) // 201 Created + Location
                .body(guardada);                                                        // Devuelve la linea creada
    }

    // ===================== UPDATE: actualizar =====================
    @PutMapping("/actualizar/{id}")                                        // PUT /api/lineas-aereas/actualizar/1
    public ResponseEntity<LineaAerea> actualizar(@PathVariable Long id,    // id de la URL
                                                 @Valid @RequestBody LineaAerea datos) { // datos nuevos del JSON
        return repo.findById(id)                                           // Buscamos la linea existente
                .map(l -> {                                                // Si existe...
                    l.setCodigoLinea(datos.getCodigoLinea());              //   nuevo codigo
                    l.setNombreLinea(datos.getNombreLinea());              //   nuevo nombre
                    l.setTipo(datos.getTipo());                            //   nuevo tipo
                    if (datos.getActivo() != null) {                       //   solo cambiamos "activo" si lo enviaron
                        l.setActivo(datos.getActivo());
                    }
                    return ResponseEntity.ok(repo.save(l));                //   UPDATE -> 200 OK
                })
                .orElse(ResponseEntity.notFound().build());                // Si no existe -> 404
    }

    // ===================== DELETE: eliminar =====================
    @DeleteMapping("/eliminar/{id}")                                  // DELETE /api/lineas-aereas/eliminar/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {     // Sin cuerpo en la respuesta
        if (!repo.existsById(id)) {                                   // Si no existe...
            return ResponseEntity.notFound().build();                 // ...404
        }
        repo.deleteById(id);                                          // DELETE FROM lineas_aereas WHERE id = ?
        return ResponseEntity.noContent().build();                    // 204 No Content
    }
}
