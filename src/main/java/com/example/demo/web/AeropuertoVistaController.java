package com.example.demo.web;

import com.example.demo.aeropuerto.Aeropuerto;             // Entidad
import com.example.demo.aeropuerto.AeropuertoRepository;  // Repositorio
import jakarta.validation.Valid;                          // Activa las validaciones
import lombok.RequiredArgsConstructor;                    // Constructor para campos final
import org.springframework.stereotype.Controller;         // Devuelve VISTAS, no JSON
import org.springframework.ui.Model;                      // Datos para la plantilla
import org.springframework.validation.BindingResult;      // Resultado de validacion
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Mensajes flash

import java.util.Objects;

// =====================================================================
//  AEROPUERTOS: estilo "Thymeleaf clasico" (MVC del lado del servidor)
// =====================================================================
@Controller
@RequestMapping("/aeropuertos")   // Prefijo de las paginas (la API sigue en /api/aeropuertos)
@RequiredArgsConstructor
public class AeropuertoVistaController {

    private final AeropuertoRepository repo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping                                       // GET /aeropuertos
    public String listar(Model model) {
        model.addAttribute("aeropuerto", new Aeropuerto()); // Objeto vacio para th:object
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con un aeropuerto para editar =====================
    @GetMapping("/{id}/editar")                       // GET /aeropuertos/3/editar
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(a -> {
                    model.addAttribute("aeropuerto", a);   // El formulario aparece lleno
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe el aeropuerto con id " + id);
                    return "redirect:/aeropuertos";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")                          // POST /aeropuertos/guardar
    public String guardar(@Valid @ModelAttribute("aeropuerto") Aeropuerto aeropuerto,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // Regla UNIQUE: no puede existir OTRO aeropuerto con el mismo codigoIATA
        repo.findAll().stream()
                .filter(otro -> otro.getCodigoAeropuerto().equalsIgnoreCase(aeropuerto.getCodigoAeropuerto()))
                .filter(otro -> !Objects.equals(otro.getId(), aeropuerto.getId()))
                .findFirst()
                .ifPresent(otro -> resultado.rejectValue("codigoIATA", "duplicado",
                        "Ya existe un aeropuerto con ese codigo IATA"));

        if (resultado.hasErrors()) {          // Si algo fallo, volvemos a mostrar la pagina con los errores
            return cargarPagina(model);       // th:errors="*{codigoIATA}" pinta cada mensaje
        }

        if (aeropuerto.getId() == null) {     // Sin id -> es NUEVO
            repo.save(aeropuerto);            // INSERT INTO aeropuertos ...
            flash.addFlashAttribute("ok", "Aeropuerto \"" + aeropuerto.getCodigoAeropuerto() + "\" creado");
        } else {                              // Con id -> ACTUALIZAR
            Aeropuerto actual = repo.findById(aeropuerto.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "El aeropuerto ya no existe");
                return "redirect:/aeropuertos";
            }
            actual.setCodigoAeropuerto(aeropuerto.getCodigoAeropuerto());
            actual.setNombre(aeropuerto.getNombre());
            actual.setCiudad(aeropuerto.getCiudad());
            actual.setPais(aeropuerto.getPais());
            actual.setActivo(aeropuerto.getActivo());
            repo.save(actual);                // UPDATE aeropuertos SET ... WHERE id = ?
            flash.addFlashAttribute("ok", "Aeropuerto \"" + actual.getCodigoAeropuerto() + "\" actualizado");
        }
        return "redirect:/aeropuertos";       // Patron Post/Redirect/Get
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")                    // POST /aeropuertos/3/eliminar
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe el aeropuerto con id " + id);
        } else {
            // OJO: cuando existan ProgramasVuelo/EscalaTecnica con FK a aeropuertos,
            // habra que revisar antes (igual que productoRepo.existsByCategoriaId en Categoria).
            repo.deleteById(id);                       // DELETE FROM aeropuertos WHERE id = ?
            flash.addFlashAttribute("ok", "Aeropuerto eliminado");
        }
        return "redirect:/aeropuertos";
    }

    // Datos comunes de la pagina: la tabla de aeropuertos
    private String cargarPagina(Model model) {
        model.addAttribute("aeropuertos", repo.findAll());
        return "aeropuertos";                          // templates/aeropuertos.html
    }
}