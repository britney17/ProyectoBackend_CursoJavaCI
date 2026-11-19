// Paquete de las VISTAS (paginas HTML con Thymeleaf)
package com.example.demo.web;

import com.example.demo.vuelo.Vuelo;
import com.example.demo.vuelo.VueloRepository;
import com.example.demo.avion.Avion;
import com.example.demo.avion.AvionRepository;
import com.example.demo.programavuelo.ProgramaVuelo;
import com.example.demo.programavuelo.ProgramaVueloRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/vuelos")   // API REST sigue en /api/vuelos
@RequiredArgsConstructor
public class VueloVistaController {

    private final VueloRepository repo;
    private final AvionRepository avionRepo;
    private final ProgramaVueloRepository programaRepo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping                                       // GET /vuelo
    public String listar(Model model) {
        model.addAttribute("vuelo", new Vuelo());
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con un vuelo para editar =====================
    @GetMapping("/{id}/editar")                       // GET /vuelo/3/editar
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(v -> {
                    model.addAttribute("vuelo", v);
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe el vuelo con id " + id);
                    return "redirect:/vuelos";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")                          // POST /vuelo/guardar
    public String guardar(@Valid @ModelAttribute("vuelo") Vuelo vuelo,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // 1. UNIQUE: no puede existir OTRO vuelo con el mismo codigoVuelo
        Vuelo otro = repo.findByCodigoVuelo(vuelo.getCodigoVuelo());
        if (otro != null && !Objects.equals(otro.getId(), vuelo.getId())) {
            resultado.rejectValue("codigoVuelo", "duplicado",
                    "Ya existe un vuelo con ese codigo");
        }

        // 2. Validar relaciones
        if (vuelo.getAvion() == null || vuelo.getAvion().getId() == null) {
            resultado.rejectValue("avion", "vacio", "Debe seleccionar un avion");
        } else if (avionRepo.findById(vuelo.getAvion().getId()).isEmpty()) {
            resultado.rejectValue("avion", "noexiste", "El avion seleccionado no existe");
        }

        if (vuelo.getProgramaVuelo() == null || vuelo.getProgramaVuelo().getId() == null) {
            resultado.rejectValue("programaVuelo", "vacio", "Debe seleccionar un programa de vuelo");
        } else if (programaRepo.findById(vuelo.getProgramaVuelo().getId()).isEmpty()) {
            resultado.rejectValue("programaVuelo", "noexiste", "El programa seleccionado no existe");
        }

        if (resultado.hasErrors()) {
            return cargarPagina(model);
        }

        // 3. Guardar o actualizar
        if (vuelo.getId() == null) {
            if (vuelo.getActivo() == null) vuelo.setActivo(true);
            repo.save(vuelo);
            flash.addFlashAttribute("ok",
                    "Vuelo \"" + vuelo.getCodigoVuelo() + "\" creado");
        } else {
            Vuelo actual = repo.findById(vuelo.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "El vuelo ya no existe");
                return "redirect:/vuelos";
            }
            actual.setCodigoVuelo(vuelo.getCodigoVuelo());
            actual.setFechaVuelo(vuelo.getFechaVuelo());
            actual.setPlazasVacias(vuelo.getPlazasVacias());
            actual.setAvion(vuelo.getAvion());
            actual.setProgramaVuelo(vuelo.getProgramaVuelo());
            actual.setActivo(vuelo.getActivo());
            repo.save(actual);
            flash.addFlashAttribute("ok",
                    "Vuelo \"" + actual.getCodigoVuelo() + "\" actualizado");
        }
        return "redirect:/vuelos";
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")                    // POST /vuelo/3/eliminar
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe el vuelo con id " + id);
        } else {
            repo.deleteById(id);
            flash.addFlashAttribute("ok", "Vuelo eliminado");
        }
        return "redirect:/vuelos";
    }

    // Datos comunes de la pagina
    private String cargarPagina(Model model) {
        model.addAttribute("vuelos", repo.findAll());          // lista para la tabla
        model.addAttribute("aviones", avionRepo.findAll());    // para el select 1
        model.addAttribute("programas", programaRepo.findAll());// para el select 2
        return "vuelos";
    }
}