package com.example.EstacionamientoUTP.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_vehiculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;
}
