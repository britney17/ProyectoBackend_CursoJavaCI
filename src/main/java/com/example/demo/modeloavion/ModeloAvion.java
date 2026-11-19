// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.modeloavion;

// Anotaciones de JPA: @Entity, @Table, @Id, @Column, @GeneratedValue
import jakarta.persistence.*;
// Validaciones: se revisan cuando el controller usa @Valid
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
// Lombok: genera codigo repetitivo al compilar
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Para dinero/capacidad con decimales exactos

@Entity                          // Esta clase es una ENTIDAD: JPA la mapea a una tabla
@Table(name = "modelo_aviones")  // Nombre de la tabla en la BD
@Data                            // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor               // Lombok: constructor vacio (obligatorio para JPA)
public class ModeloAvion {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El codigo del modelo es obligatorio")
    @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)  // NOT NULL y UNIQUE
    private String codigoModelo;

    @NotNull(message = "El numero de plazas es obligatorio")
    @Positive(message = "El numero de plazas debe ser mayor que 0")
    @Column(nullable = false)                              // NOT NULL
    private Integer numeroPlazas;

    @NotNull(message = "La capacidad de carga es obligatoria")
    @PositiveOrZero(message = "La capacidad de carga no puede ser negativa")
    @Column(nullable = false, precision = 10, scale = 2)   // decimal(10,2)
    private BigDecimal capacidadCarga;

    @Column(name = "activo") // Igual que en las demas entidades
    private Boolean activo = true; // Valor por defecto: si el JSON no lo envia, queda en true

    // Constructor para crear modelos sin id:
    // new ModeloAvion(null, "B737", 180, new BigDecimal("20.50"), true)
    public ModeloAvion(Long id, String codigoModelo, Integer numeroPlazas,
                       BigDecimal capacidadCarga, Boolean activo) {
        this.id = id;
        this.codigoModelo = codigoModelo;
        this.numeroPlazas = numeroPlazas;
        this.capacidadCarga = capacidadCarga;
        this.activo = activo;
    }
}