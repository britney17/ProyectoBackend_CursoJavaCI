// Paquete de las VISTAS (paginas HTML con Thymeleaf)
package com.example.demo.web;

import com.example.demo.modeloavion.ModeloAvion;             // Entidad
import com.example.demo.modeloavion.ModeloAvionRepository;  // Repositorio
import jakarta.validation.Valid;                          // Activa las validaciones
import lombok.RequiredArgsConstructor;                    // Constructor para campos final
import org.springframework.stereotype.Controller;         // Devuelve VISTAS, no JSON
import org.springframework.ui.Model;                      // Datos para la plantilla
import org.springframework.validation.BindingResult;      // Resultado de validacion
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Mensajes flash

import java.util.Objects;

@Controller
@RequestMapping("/modeloavion")   // La API REST sigue en /api/modelos-avion
@RequiredArgsConstructor
public class ModeloAvionVistaController {

    private final ModeloAvionRepository repo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping                                       // GET /modelo_avion
    public String listar(Model model) {
        model.addAttribute("modeloAvion", new ModeloAvion()); // Objeto vacio para th:object
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con un modelo para editar =====================
    @GetMapping("/{id}/editar")                       // GET /modelo_avion/3/editar
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(m -> {
                    model.addAttribute("modeloAvion", m);
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe el modelo con id " + id);
                    return "redirect:/modeloavion";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")                          // POST /modelo_avion/guardar
    public String guardar(@Valid @ModelAttribute("modeloAvion") ModeloAvion modelo,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // UNIQUE: no puede existir OTRO modelo con el mismo codigoModelo
        ModeloAvion otro = repo.findByCodigoModelo(modelo.getCodigoModelo());
        if (otro != null && !Objects.equals(otro.getId(), modelo.getId())) {
            resultado.rejectValue("codigoModelo", "duplicado",
                    "Ya existe un modelo con ese codigo");
        }

        if (resultado.hasErrors()) {
            return cargarPagina(model);
        }

        if (modelo.getId() == null) {           // Sin id -> es NUEVO
            if (modelo.getActivo() == null) modelo.setActivo(true);
            repo.save(modelo);
            flash.addFlashAttribute("ok",
                    "Modelo \"" + modelo.getCodigoModelo() + "\" creado");
        } else {                                // Con id -> ACTUALIZAR
            ModeloAvion actual = repo.findById(modelo.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "El modelo ya no existe");
                return "redirect:/modeloavion";
            }
            actual.setCodigoModelo(modelo.getCodigoModelo());
            actual.setNumeroPlazas(modelo.getNumeroPlazas());
            actual.setCapacidadCarga(modelo.getCapacidadCarga());
            actual.setActivo(modelo.getActivo());
            repo.save(actual);
            flash.addFlashAttribute("ok",
                    "Modelo \"" + actual.getCodigoModelo() + "\" actualizado");
        }
        return "redirect:/modeloavion";
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")                    // POST /modelo_avion/3/eliminar
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe el modelo con id " + id);
        } else {
            // OJO: cuando exista Avion con FK a ModeloAvion, validar antes.
            repo.deleteById(id);
            flash.addFlashAttribute("ok", "Modelo eliminado");
        }
        return "redirect:/modeloavion";
    }

    // Datos comunes de la pagina
    private String cargarPagina(Model model) {
        model.addAttribute("modelos", repo.findAll());
        return "modeloavion";   // templates/modelo_avion.html
    }
}