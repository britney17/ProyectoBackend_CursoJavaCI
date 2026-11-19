// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.vuelo;

// Entidades del otro lado de las relaciones
import com.example.demo.avion.Avion;
import com.example.demo.programavuelo.ProgramaVuelo;
// Anotaciones de JPA
import jakarta.persistence.*;
// Validaciones
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
// Lombok
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate; // Para la fecha del vuelo (mapea a DATE en BD)

@Entity                    // Esta clase es una ENTIDAD
@Table(name = "vuelos")    // Nombre de la tabla en la BD
@Data                      // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor         // Lombok: constructor vacio (obligatorio para JPA)
public class Vuelo {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El codigo del vuelo es obligatorio")
    @Size(max = 15, message = "El codigo no puede superar 15 caracteres")
    @Column(nullable = false, unique = true, length = 15)  // NOT NULL y UNIQUE
    private String codigoVuelo;

    @NotNull(message = "La fecha del vuelo es obligatoria")
    @Column(nullable = false)                              // NOT NULL (DATE)
    private LocalDate fechaVuelo;

    @NotNull(message = "Las plazas vacias son obligatorias (pueden ser 0)")
    @PositiveOrZero(message = "Las plazas vacias no pueden ser negativas")
    @Column(nullable = false)                              // NOT NULL
    private Integer plazasVacias;

    // ===================== RELACION: muchos vuelos -> un avion =====================
    @ManyToOne
    @JoinColumn(name = "avion_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Avion avion;

    // ===================== RELACION: muchos vuelos -> un programa =====================
    // Un programa recurrente (con dias de la semana) genera MUCHOS vuelos concretos (con fecha).
    // En la BD: vuelos.programa_vuelo_id --> programa_vuelos.id
    @ManyToOne
    @JoinColumn(name = "programa_vuelo_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProgramaVuelo programaVuelo;

    @Column(name = "activo") // Igual que en las demas entidades
    private Boolean activo = true; // Valor por defecto

    // Constructor para crear vuelos sin id:
    // new Vuelo(null, "AV100-2026-01-15", LocalDate.of(2026,1,15), 12, avion, programa, true)
    public Vuelo(Long id, String codigoVuelo, LocalDate fechaVuelo, Integer plazasVacias,
                 Avion avion, ProgramaVuelo programaVuelo, Boolean activo) {
        this.id = id;
        this.codigoVuelo = codigoVuelo;
        this.fechaVuelo = fechaVuelo;
        this.plazasVacias = plazasVacias;
        this.avion = avion;
        this.programaVuelo = programaVuelo;
        this.activo = activo;
    }
}