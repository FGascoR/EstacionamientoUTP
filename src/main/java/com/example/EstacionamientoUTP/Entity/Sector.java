package com.example.EstacionamientoUTP.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sectores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String img;

    private String nombre;

    @Column(name = "espacios_disponibles")
    private int espaciosDisponibles;

    @Column(name = "espacios_ocupados")
    private int espaciosOcupados;

    private String estado;
}
