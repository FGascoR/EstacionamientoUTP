package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.*;
import com.example.EstacionamientoUTP.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.example.EstacionamientoUTP.Security.CustomUserDetails;

import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // <-- 1. ESTE IMPORT FALTABA

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservas")
public class ReservaController {

    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final SectorRepository sectorRepository;
    private final EspacioRepository espacioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;

    @GetMapping("/nueva")
    public String nuevaReserva(
            @RequestParam("tipoVehiculo") Long idTipoVehiculo,
            Model model) {

        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.findById(idTipoVehiculo).orElse(null);

        model.addAttribute("tipoSeleccionado", tipoVehiculo);
        model.addAttribute("sectores", sectorRepository.findAll());

        return "ReservaForm";
    }

    @GetMapping("/espacios")
    @ResponseBody
    public List<Espacio> obtenerEspacios(
            @RequestParam("sectorId") Long sectorId,
            @RequestParam("tipoVehiculoId") Long tipoVehiculoId) {

        return espacioRepository.findBySector_IdAndTipoVehiculo_Id(sectorId, tipoVehiculoId);
    }

    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardarReserva(
            @RequestParam Long sectorId,
            @RequestParam Long espacioId,
            @RequestParam String placa,
            @RequestParam String fecha,
            @RequestParam String entrada,
            @RequestParam String salida,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuarioEntity();

            Espacio espacio = espacioRepository.findById(espacioId)
                    .orElseThrow(() -> new RuntimeException("Espacio no encontrado"));
            Sector sector = espacio.getSector();

            if (!"LIBRE".equalsIgnoreCase(espacio.getEstado())) {
                resp.put("success", false);
                resp.put("error", "ocupado");
                return resp;
            }

            espacio.setEstado("OCUPADO");
            espacioRepository.save(espacio);

            sector.setEspaciosDisponibles(sector.getEspaciosDisponibles() - 1);
            sector.setEspaciosOcupados(sector.getEspaciosOcupados() + 1);
            sectorRepository.save(sector);

            Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa)
                    .orElseGet(() -> {
                        Vehiculo nuevo = Vehiculo.builder()
                                .placa(placa)
                                .tipoVehiculo(espacio.getTipoVehiculo())
                                .usuario(usuario)
                                .build();
                        return vehiculoRepository.save(nuevo);
                    });

            Reserva reserva = Reserva.builder()
                    .sector(sector)
                    .espacio(espacio)
                    .vehiculo(vehiculo)
                    .usuario(usuario)
                    .fechaReserva(LocalDate.parse(fecha))
                    .horaEntrada(LocalTime.parse(entrada))
                    .horaSalida(LocalTime.parse(salida))
                    .build();
            reservaRepository.save(reserva);

            resp.put("success", true);
            resp.put("espacioId", espacioId);
            resp.put("sector", Map.of(
                    "id", sector.getId(),
                    "disponibles", sector.getEspaciosDisponibles(),
                    "ocupados", sector.getEspaciosOcupados()
            ));
            resp.put("totales", Map.of(
                    "disponibles", sectorRepository.findAll().stream().mapToInt(Sector::getEspaciosDisponibles).sum(),
                    "ocupados", sectorRepository.findAll().stream().mapToInt(Sector::getEspaciosOcupados).sum()
            ));

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HH:mm");

            resp.put("nuevaReserva", Map.of(
                    "fecha", reserva.getFechaReserva().format(dtfFecha),
                    "entrada", reserva.getHoraEntrada().format(dtfHora),
                    "salida", reserva.getHoraSalida().format(dtfHora),
                    "sector", reserva.getSector().getNombre(),
                    "espacio", reserva.getEspacio().getNombre(),
                    "estado", "En Proceso"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", e.getMessage());
        }

        return resp;
    }

    @GetMapping("/placas")
    @ResponseBody
    public List<Vehiculo> obtenerPlacasPorTipo(
            @RequestParam Long tipoVehiculoId,
            Authentication auth) {

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Usuario usuario = userDetails.getUsuarioEntity();

        return vehiculoRepository.findByUsuarioIdAndTipoVehiculoId(usuario.getId(), tipoVehiculoId);
    }
}