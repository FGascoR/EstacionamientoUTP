package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.Reserva;
import com.example.EstacionamientoUTP.Entity.Espacio;
import com.example.EstacionamientoUTP.Entity.Sector;
import com.example.EstacionamientoUTP.Entity.Vehiculo;
import com.example.EstacionamientoUTP.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReservaController {

    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final SectorRepository sectorRepository;
    private final VehiculoRepository vehiculoRepository;

    @GetMapping("/reservas")
    public String verReservas(Model model) {
        List<Reserva> reservas = reservaRepository.findAll();
        model.addAttribute("reservas", reservas);
        return "PanelAdministrativo";
    }

    @DeleteMapping("/reservas/eliminar/{id}")
    @ResponseBody
    public String eliminarReserva(@PathVariable Long id) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            Espacio espacio = reserva.getEspacio();
            Sector sector = espacio.getSector();

            espacio.setEstado("LIBRE");
            espacioRepository.save(espacio);

            sector.setEspaciosDisponibles(sector.getEspaciosDisponibles() + 1);
            sector.setEspaciosOcupados(sector.getEspaciosOcupados() - 1);
            sectorRepository.save(sector);

            reservaRepository.delete(reserva);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

}
