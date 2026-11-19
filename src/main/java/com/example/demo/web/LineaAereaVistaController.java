// Paquete de las VISTAS (paginas HTML con Thymeleaf)
package com.example.demo.web;

import com.example.demo.linea_aerea.LineaAerea;             // Entidad
import com.example.demo.linea_aerea.LineaAereaRepository;  // Repositorio
import jakarta.validation.Valid;                          // Activa las validaciones
import lombok.RequiredArgsConstructor;                    // Constructor para campos final
import org.springframework.stereotype.Controller;         // Devuelve VISTAS, no JSON
import org.springframework.ui.Model;                      // Datos para la plantilla
import org.springframework.validation.BindingResult;      // Resultado de validacion
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Mensajes flash

import java.util.Objects;

@Controller
@RequestMapping("/linea_aerea")   // La API sigue en /api/lineas-aereas
@RequiredArgsConstructor
public class LineaAereaVistaController {

    private final LineaAereaRepository repo;

    // ===================== Listado + formulario vacio =====================
    @GetMapping                                       // GET /lineas-aereas
    public String listar(Model model) {
        model.addAttribute("lineaAerea", new LineaAerea()); // Objeto vacio para th:object
        return cargarPagina(model);
    }

    // ===================== Listado + formulario con una linea para editar =====================
    @GetMapping("/{id}/editar")                       // GET /lineas-aereas/3/editar
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return repo.findById(id)
                .map(l -> {
                    model.addAttribute("lineaAerea", l);
                    return cargarPagina(model);
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "No existe la linea aerea con id " + id);
                    return "redirect:/linea_aerea";
                });
    }

    // ===================== Guardar (crear o actualizar) =====================
    @PostMapping("/guardar")                          // POST /lineas-aereas/guardar
    public String guardar(@Valid @ModelAttribute("lineaAerea") LineaAerea linea,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes flash) {

        // UNIQUE: no puede existir OTRA linea con el mismo codigoLinea
      
        LineaAerea otra = repo.findByCodigoLinea(linea.getCodigoLinea());
        if (otra != null && !Objects.equals(otra.getId(), linea.getId())) {
            resultado.rejectValue("codigoLinea", "duplicado",
                    "Ya existe una linea aerea con ese codigo");
}
        if (resultado.hasErrors()) {
            return cargarPagina(model);
        }

        if (linea.getId() == null) {           // Sin id -> es NUEVA
            if (linea.getActivo() == null) linea.setActivo(true);
            repo.save(linea);
            flash.addFlashAttribute("ok",
                    "Linea aerea \"" + linea.getCodigoLinea() + "\" creada");
        } else {                               // Con id -> ACTUALIZAR
            LineaAerea actual = repo.findById(linea.getId()).orElse(null);
            if (actual == null) {
                flash.addFlashAttribute("error", "La linea aerea ya no existe");
                return "redirect:/linea_aerea";
            }
            actual.setCodigoLinea(linea.getCodigoLinea());
            actual.setNombreLinea(linea.getNombreLinea());
            actual.setTipo(linea.getTipo());
            actual.setActivo(linea.getActivo());
            repo.save(actual);
            flash.addFlashAttribute("ok",
                    "Linea aerea \"" + actual.getCodigoLinea() + "\" actualizada");
        }
        return "redirect:/linea_aerea";
    }

    // ===================== Eliminar =====================
    @PostMapping("/{id}/eliminar")                    // POST /lineas-aereas/3/eliminar
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        if (!repo.existsById(id)) {
            flash.addFlashAttribute("error", "No existe la linea aerea con id " + id);
        } else {
            // OJO: cuando existan ProgramaVuelo con FK a LineaAerea, validar antes.
            repo.deleteById(id);
            flash.addFlashAttribute("ok", "Linea aerea eliminada");
        }
        return "redirect:/linea_aerea";
    }

    // Datos comunes de la pagina
    private String cargarPagina(Model model) {
        model.addAttribute("lineas", repo.findAll());
        return "linea_aerea";   // templates/lineas-aereas.html
    }
}