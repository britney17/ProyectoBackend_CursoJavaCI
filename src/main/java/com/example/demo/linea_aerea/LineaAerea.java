package com.example.demo.linea_aerea;

import jakarta.persistence.*;

import java.lang.annotation.Inherited;

import javax.annotation.processing.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineasAereas")
@Data
@NoArgsConstructor

public class LineaAerea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //No se acepta valores vacio
    @NotBlank(message = "El codigo de la linea es obligatorio")
    @Size(max = 10, message = "El codigo no puede superar 10 caracteres")
    @Column(nullable = false, unique = true, length = 10)
    private String codigoLinea;            

    @NotBlank(message = "El nombre de la linea es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)                
    private String nombreLinea; 
    
    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 30, message = "El tipo no puede superar 30 caracteres")
    @Column(nullable = false, length = 30)                     
    private String tipo;

    @Column(name = "activo") 
    private Boolean activo = true; // Valor por defecto: si el JSON no lo envia, queda en true

    // Constructor para crear lineas sin id: new LineaAerea(null, "AV", "Avianca", "Internacional", true)
    public LineaAerea(Long id, String codigoLinea, String nombreLinea, String tipo, Boolean activo) {
        this.id = id;
        this.codigoLinea = codigoLinea;
        this.nombreLinea = nombreLinea;
        this.tipo = tipo;
        this.activo = activo;
    }
}



    
