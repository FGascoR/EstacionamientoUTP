package com.example.EstacionamientoUTP.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class VehiculoDTO {
    private Long id;
    private String placa;
    private String tipoVehiculoNombre;
}
