package com.example.EstacionamientoUTP.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_espacios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubEspacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "espacio_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Espacio espacio;

    @ManyToOne
    @JoinColumn(name = "tipo_vehiculo_id", nullable = false)
    private TipoVehiculo tipoVehiculo;
}