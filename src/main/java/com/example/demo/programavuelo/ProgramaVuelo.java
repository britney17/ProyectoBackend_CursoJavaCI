// Paquete donde vive la clase (debe coincidir con la carpeta)
package com.example.demo.programavuelo;

// Entidades del otro lado de las relaciones
import com.example.demo.aeropuerto.Aeropuerto;
import com.example.demo.linea_aerea.LineaAerea;// Anotaciones de JPA
import jakarta.persistence.*;
// Validaciones
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
// Lombok
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity                          // Esta clase es una ENTIDAD
@Table(name = "programavuelo") // Nombre de la tabla en la BD
@Data                            // Lombok: getters, setters, toString, equals y hashCode
@NoArgsConstructor               // Lombok: constructor vacio (obligatorio para JPA)
public class ProgramaVuelo {

    @Id                                                 // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @NotBlank(message = "El codigo del programa es obligatorio")
    @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)  // NOT NULL y UNIQUE
    private String codigoPro;

    @NotBlank(message = "El numero de vuelo es obligatorio")
    @Size(max = 10, message = "El numero de vuelo no puede superar 10 caracteres")
    @Column(nullable = false, length = 10)                 // NOT NULL
    private String numeVuelo;

    @NotBlank(message = "Los dias de la semana son obligatorios")
    @Size(max = 20, message = "Los dias no pueden superar 20 caracteres")
    @Column(nullable = false, length = 20)                 // NOT NULL (ej. "L,M,X,J,V")
    private String diasSemana;

    // ===================== RELACION: muchos programas -> una linea aerea =====================
    @ManyToOne
    @JoinColumn(name = "linea_aerea_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LineaAerea lineaAerea;

    // ===================== RELACION: muchos programas -> un aeropuerto ORIGEN =====================
    @ManyToOne
    @JoinColumn(name = "aeropuerto_origen_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Aeropuerto aeropuertoOrigen;

    // ===================== RELACION: muchos programas -> un aeropuerto DESTINO =====================
    @ManyToOne
    @JoinColumn(name = "aeropuerto_destino_id", nullable = false) // FK y NOT NULL
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Aeropuerto aeropuertoDestino;

    @Column(name = "activo") // Igual que en las demas entidades
    private Boolean activo = true; // Valor por defecto

    // Constructor para crear programas sin id:
    // new ProgramaVuelo(null, "PRO-001", "AV100", "L,M,X", linea, origen, destino, true)
    public ProgramaVuelo(Long id, String codigoPro, String numeVuelo, String diasSemana,
                         LineaAerea lineaAerea, Aeropuerto aeropuertoOrigen,
                         Aeropuerto aeropuertoDestino, Boolean activo) {
        this.id = id;
        this.codigoPro = codigoPro;
        this.numeVuelo = numeVuelo;
        this.diasSemana = diasSemana;
        this.lineaAerea = lineaAerea;
        this.aeropuertoOrigen = aeropuertoOrigen;
        this.aeropuertoDestino = aeropuertoDestino;
        this.activo = activo;
    }
}