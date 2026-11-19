// Paquete de las VISTAS (paginas HTML con Thymeleaf)
package com.example.demo.web;

import com.example.demo.programavuelo.ProgramaVuelo;
import com.example.demo.programavuelo.ProgramaVueloRepository;
import com.example.demo.linea_aerea.LineaAerea;           // o linea_aerea, segun tu paquete
import com.example.demo.linea_aerea.LineaAereaRepository; // o linea_aerea
import com.example.demo.aeropuerto.Aeropuerto;
import com.example.demo.aeropuerto.AeropuertoRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/programavuelo")   // API REST sigue en /api/programas-vuelo
@RequiredArgsConstructor
public class ProgramaVueloVistaController {

    private final ProgramaVueloRepository repo;
    private final LineaAereaRepository lineaRepo;
    private final AeropuertoRepository aeropuertoRepo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping                                       // GET /programavuelo
    public String listar(Model model) {
        model.addAttribute("programaVuelo", new ProgramaVuelo());
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con un programa para editar =====================
    @GetMapping("/{id}/editar")                       // GET /programavuelo/3/editar
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(p -> {
                    model.addAttribute("programaVuelo", p);
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe el programa con id " + id);
                    return "redirect:/programavuelo";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")                          // POST /programavuelo/guardar
    public String guardar(@Valid @ModelAttribute("programaVuelo") ProgramaVuelo programa,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // 1. UNIQUE: no puede existir OTRO programa con el mismo codigoPro
        ProgramaVuelo otro = repo.findByCodigoPro(programa.getCodigoPro());
        if (otro != null && !Objects.equals(otro.getId(), programa.getId())) {
            resultado.rejectValue("codigoPro", "duplicado",
                    "Ya existe un programa con ese codigo");
        }

        // 2. Validar las 3 relaciones
        if (programa.getLineaAerea() == null || programa.getLineaAerea().getId() == null) {
            resultado.rejectValue("lineaAerea", "vacio", "Debe seleccionar una linea aerea");
        } else if (lineaRepo.findById(programa.getLineaAerea().getId()).isEmpty()) {
            resultado.rejectValue("lineaAerea", "noexiste", "La linea aerea seleccionada no existe");
        }

        if (programa.getAeropuertoOrigen() == null || programa.getAeropuertoOrigen().getId() == null) {
            resultado.rejectValue("aeropuertoOrigen", "vacio", "Debe seleccionar un aeropuerto de origen");
        } else if (aeropuertoRepo.findById(programa.getAeropuertoOrigen().getId()).isEmpty()) {
            resultado.rejectValue("aeropuertoOrigen", "noexiste", "El aeropuerto de origen no existe");
        }

        if (programa.getAeropuertoDestino() == null || programa.getAeropuertoDestino().getId() == null) {
            resultado.rejectValue("aeropuertoDestino", "vacio", "Debe seleccionar un aeropuerto de destino");
        } else if (aeropuertoRepo.findById(programa.getAeropuertoDestino().getId()).isEmpty()) {
            resultado.rejectValue("aeropuertoDestino", "noexiste", "El aeropuerto de destino no existe");
        }

        // 3. Regla de negocio: origen y destino no pueden ser el mismo
        if (programa.getAeropuertoOrigen() != null && programa.getAeropuertoOrigen().getId() != null
                && programa.getAeropuertoDestino() != null && programa.getAeropuertoDestino().getId() != null
                && programa.getAeropuertoOrigen().getId().equals(programa.getAeropuertoDestino().getId())) {
            resultado.rejectValue("aeropuertoDestino", "iguales",
                    "El aeropuerto de destino no puede ser el mismo que el de origen");
        }

        if (resultado.hasErrors()) {
            return cargarPagina(model);
        }

        // 4. Guardar o actualizar
        if (programa.getId() == null) {
            if (programa.getActivo() == null) programa.setActivo(true);
            repo.save(programa);
            flash.addFlashAttribute("ok",
                    "Programa \"" + programa.getCodigoPro() + "\" creado");
        } else {
            ProgramaVuelo actual = repo.findById(programa.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "El programa ya no existe");
                return "redirect:/programavuelo";
            }
            actual.setCodigoPro(programa.getCodigoPro());
            actual.setNumeVuelo(programa.getNumeVuelo());
            actual.setDiasSemana(programa.getDiasSemana());
            actual.setLineaAerea(programa.getLineaAerea());
            actual.setAeropuertoOrigen(programa.getAeropuertoOrigen());
            actual.setAeropuertoDestino(programa.getAeropuertoDestino());
            actual.setActivo(programa.getActivo());
            repo.save(actual);
            flash.addFlashAttribute("ok",
                    "Programa \"" + actual.getCodigoPro() + "\" actualizado");
        }
        return "redirect:/programavuelo";
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")                    // POST /programavuelo/3/eliminar
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe el programa con id " + id);
        } else {
            repo.deleteById(id);
            flash.addFlashAttribute("ok", "Programa eliminado");
        }
        return "redirect:/programavuelo";
    }

    // Datos comunes de la pagina
    private String cargarPagina(Model model) {
        model.addAttribute("programas", repo.findAll());              // lista para la tabla
        model.addAttribute("lineas", lineaRepo.findAll());            // para el select 1
        model.addAttribute("aeropuertos", aeropuertoRepo.findAll());  // para los selects 2 y 3
        return "programavuelo";
    }
}