// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.aeropuerto;

// Anotaciones de JPA: @Entity, @Table, @Id, @Column, @GeneratedValue
import jakarta.persistence.*;
// Validaciones: se revisan cuando el controller usa @Valid
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// Lombok: genera codigo repetitivo al compilar
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity                        // Esta clase es una ENTIDAD: JPA la mapea a una tabla
@Table(name = "aeropuertos")   // Nombre de la tabla en la BD
@Data                          // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor             // Lombok: constructor vacio (obligatorio para JPA)
public class Aeropuerto {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El codigo del aereopuerto es obligatorio")        // No puede venir vacio
    @Size(min = 3, max = 3, message = "El codigo aereopuerto debe tener 3 caracteres")
    @Column(nullable = false, unique = true, length = 3)        // NOT NULL y UNIQUE
    private String codigoAeropuerto;

    @NotBlank(message = "El nombre del aeropuerto es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)                     // NOT NULL
    private String nombre;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    @Column(nullable = false, length = 80)                      // NOT NULL
    private String ciudad;

    @NotBlank(message = "El pais es obligatorio")
    @Size(max = 80, message = "El pais no puede superar 80 caracteres")
    @Column(nullable = false, length = 80)                      // NOT NULL
    private String pais;

    @Column(name = "activo") 
    private Boolean activo = true; // Valor por defecto: si el JSON no lo envia, queda en true

    // Constructor para crear aeropuertos sin id:
    // new Aeropuerto(null, "MGA", "Augusto C. Sandino", "Managua", "Nicaragua", true)
    public Aeropuerto(Long id, String codigoAeropuerto, String nombre, String ciudad, String pais, Boolean activo) {
        this.id = id;
        this.codigoAeropuerto = codigoAeropuerto;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.pais = pais;
        this.activo = activo;
    }
}