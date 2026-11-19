// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.avion;

// Entidad del otro lado de la relacion
import com.example.demo.modeloavion.ModeloAvion;
// Anotaciones de JPA: @Entity, @Table, @Id, @Column, @GeneratedValue, @ManyToOne, @JoinColumn
import jakarta.persistence.*;
// Validaciones: se revisan cuando el controller usa @Valid
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
// Lombok: genera codigo repetitivo al compilar
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity                    // Esta clase es una ENTIDAD: JPA la mapea a una tabla
@Table(name = "aviones")   // Nombre de la tabla en la BD
@Data                      // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor         // Lombok: constructor vacio (obligatorio para JPA)
public class Avion {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El numero de serie es obligatorio")
    @Size(max = 30, message = "El numero de serie no puede superar 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)  // NOT NULL y UNIQUE
    private String numeroSerie;

    @NotBlank(message = "El nombre del avion es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    @Column(nullable = false, length = 80)                 // NOT NULL
    private String nombre;

    // ===================== RELACION: muchos aviones -> un modelo =====================
    // @ManyToOne: MUCHOS aviones pueden ser del MISMO modelo.
    // En la BD esto se traduce en una FK en la tabla "aviones":
    //
    //   aviones (id, numero_serie, nombre, modelo_avion_id) --> modelo_aviones (id)
    //
    // Por defecto es EAGER en @ManyToOne: al leer un avion, JPA trae su modelo.
    @ManyToOne
    @JoinColumn(name = "modelo_avion_id", nullable = false) // Nombre de la FK y NOT NULL
    @ToString.Exclude                                       // Lombok: no incluir en toString()
    @EqualsAndHashCode.Exclude                              // Lombok: no incluir en equals()/hashCode()
    private ModeloAvion modeloAvion;

    @Column(name = "activo") // Igual que en las entidades anteriores
    private Boolean activo = true; // Valor por defecto: si el JSON no lo envia, queda en true

    // Constructor para crear aviones sin id:
    // new Avion(null, "N12345", "El Condor", modelo, true)
    public Avion(Long id, String numeroSerie, String nombre, ModeloAvion modeloAvion, Boolean activo) {
        this.id = id;
        this.numeroSerie = numeroSerie;
        this.nombre = nombre;
        this.modeloAvion = modeloAvion;
        this.activo = activo;
    }
}