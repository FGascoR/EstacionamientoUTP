package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.Reserva;
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

            reservaRepository.delete(reserva);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

}