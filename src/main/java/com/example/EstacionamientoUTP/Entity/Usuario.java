package com.example.EstacionamientoUTP.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String usuario;

    @Column(nullable = false)
    private String contraseña;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas;
}
