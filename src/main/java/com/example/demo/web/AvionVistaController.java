// Paquete de las VISTAS (paginas HTML con Thymeleaf)
package com.example.demo.web;

import com.example.demo.avion.Avion;
import com.example.demo.avion.AvionRepository;
import com.example.demo.modeloavion.ModeloAvion;
import com.example.demo.modeloavion.ModeloAvionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/avion")
@RequiredArgsConstructor
public class AvionVistaController {

    private final AvionRepository repo;
    private final ModeloAvionRepository modeloRepo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("avion", new Avion());
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con un avion para editar =====================
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(a -> {
                    model.addAttribute("avion", a);
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe el avion con id " + id);
                    return "redirect:/avion";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("avion") Avion avion,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // 1. Validar UNIQUE del numero de serie
        Avion otro = repo.findByNumeroSerie(avion.getNumeroSerie());
        if (otro != null && !Objects.equals(otro.getId(), avion.getId())) {
            resultado.rejectValue("numeroSerie", "duplicado",
                    "Ya existe un avion con ese numero de serie");
        }

        // 2. Validar que el modelo seleccionado exista
        ModeloAvion modeloCompleto = null;
        if (avion.getModeloAvion() != null && avion.getModeloAvion().getId() != null) {
            modeloCompleto = modeloRepo.findById(avion.getModeloAvion().getId()).orElse(null);
            if (modeloCompleto == null) {
                resultado.rejectValue("modeloAvion", "noexiste",
                        "El modelo seleccionado ya no existe");
            }
        }

        if (resultado.hasErrors()) {
            return cargarPagina(model);
        }

        // 3. Asignar el modelo COMPLETO (no el parcial que viene del <select>)
        avion.setModeloAvion(modeloCompleto);

        // 4. Guardar o actualizar
        if (avion.getId() == null) {
            if (avion.getActivo() == null) avion.setActivo(true);
            repo.save(avion);
            flash.addFlashAttribute("ok", "Avion \"" + avion.getNumeroSerie() + "\" creado");
        } else {
            Avion actual = repo.findById(avion.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "El avion ya no existe");
                return "redirect:/avion";
            }
            actual.setNumeroSerie(avion.getNumeroSerie());
            actual.setNombre(avion.getNombre());
            actual.setModeloAvion(modeloCompleto); // Actualizamos con el modelo completo
            actual.setActivo(avion.getActivo());
            repo.save(actual);
            flash.addFlashAttribute("ok", "Avion \"" + actual.getNumeroSerie() + "\" actualizado");
        }
        return "redirect:/avion";
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe el avion con id " + id);
        } else {
            repo.deleteById(id);
            flash.addFlashAttribute("ok", "Avion eliminado");
        }
        return "redirect:/avion";
    }

    // Datos comunes de la pagina
    private String cargarPagina(Model model) {
        model.addAttribute("aviones", repo.findAll());       // Lista para la tabla (PLURAL)
        model.addAttribute("modelos", modeloRepo.findAll()); // Lista para el <select>
        return "avion";
    }
}