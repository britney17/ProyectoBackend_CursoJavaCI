// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.escalatecnica;

// Entidades del otro lado de las relaciones
import com.example.demo.aeropuerto.Aeropuerto;
import com.example.demo.vuelo.Vuelo;
// Anotaciones de JPA
import jakarta.persistence.*;
// Validaciones
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
// Lombok
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal; // Para cargas/descargas con decimales exactos

@Entity                            // Esta clase es una ENTIDAD
@Table(
    name = "escalas_tecnicas",     // Nombre de la tabla en la BD
    uniqueConstraints = @UniqueConstraint(
        name = "uq_escala_vuelo_orden",       // Nombre de la restriccion
        columnNames = {"vuelo_id", "numero_orden"} // Un vuelo no puede tener 2 escalas con el mismo orden
    )
)
@Data                              // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor                 // Lombok: constructor vacio (obligatorio para JPA)
public class EscalaTecnica {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El codigo de la escala es obligatorio")
    @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)  // NOT NULL y UNIQUE
    private String codigoEscala;

    @NotNull(message = "El numero de orden es obligatorio")
    @Positive(message = "El numero de orden debe ser mayor que 0")
    @Column(name = "numero_orden", nullable = false)       // NOT NULL (columna usada en el UNIQUE compuesto)
    private Integer numeroOrden;

    @NotNull(message = "Las cargas son obligatorias (pueden ser 0)")
    @PositiveOrZero(message = "Las cargas no pueden ser negativas")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cargas;

    @NotNull(message = "Las descargas son obligatorias (pueden ser 0)")
    @PositiveOrZero(message = "Las descargas no pueden ser negativas")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descargas;

    // ===================== RELACION: muchas escalas -> un vuelo =====================
    // Una escala tecnica pertenece a UN vuelo concreto.
    // En la BD: escalas_tecnicas.vuelo_id --> vuelos.id
    @ManyToOne
    @JoinColumn(name = "vuelo_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vuelo vuelo;

    // ===================== RELACION: muchas escalas -> un aeropuerto =====================
    // Una escala tecnica ocurre en UN aeropuerto concreto.
    // En la BD: escalas_tecnicas.aeropuerto_id --> aeropuertos.id
    @ManyToOne
    @JoinColumn(name = "aeropuerto_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Aeropuerto aeropuerto;

    @Column(name = "activo") // Igual que en las demas entidades
    private Boolean activo = true; // Valor por defecto

    // Constructor para crear escalas sin id:
    // new EscalaTecnica(null, "ESC-001", 1, new BigDecimal("5.00"), new BigDecimal("3.50"),
    //                   vuelo, aeropuerto, true)
    public EscalaTecnica(Long id, String codigoEscala, Integer numeroOrden,
                         BigDecimal cargas, BigDecimal descargas,
                         Vuelo vuelo, Aeropuerto aeropuerto, Boolean activo) {
        this.id = id;
        this.codigoEscala = codigoEscala;
        this.numeroOrden = numeroOrden;
        this.cargas = cargas;
        this.descargas = descargas;
        this.vuelo = vuelo;
        this.aeropuerto = aeropuerto;
        this.activo = activo;
    }
}