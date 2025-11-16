package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.Espacio;
import com.example.EstacionamientoUTP.Entity.Sector;
import com.example.EstacionamientoUTP.Entity.SubEspacio;
import com.example.EstacionamientoUTP.Repository.EspacioRepository;
import com.example.EstacionamientoUTP.Repository.SubEspacioRepository;
import com.example.EstacionamientoUTP.Repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/espacios")
public class EspacioController {

    private final EspacioRepository espacioRepository;
    private final SectorRepository sectorRepository;
    private final SubEspacioRepository subEspacioRepository;

    @PostMapping("/actualizarEstadoSub/{id}")
    public String actualizarEstadoSub(
            @PathVariable Long id,
            @RequestParam("nuevoEstado") String nuevoEstado
    ) {
        SubEspacio subEspacio = subEspacioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubEspacio no encontrado"));

        if (subEspacio.getEstado().equals(nuevoEstado)) {
            return "redirect:/espacios/listar";
        }

        Sector sector = subEspacio.getEspacio().getSector();

        if ("OCUPADO".equalsIgnoreCase(nuevoEstado)) {
            sector.setEspaciosOcupados(sector.getEspaciosOcupados() + 1);
            sector.setEspaciosDisponibles(sector.getEspaciosDisponibles() - 1);
        } else if ("LIBRE".equalsIgnoreCase(nuevoEstado)) {
            sector.setEspaciosOcupados(sector.getEspaciosOcupados() - 1);
            sector.setEspaciosDisponibles(sector.getEspaciosDisponibles() + 1);
        }

        subEspacio.setEstado(nuevoEstado);
        subEspacioRepository.save(subEspacio);
        sectorRepository.save(sector);

        return "redirect:/espacios/listar";
    }
}
