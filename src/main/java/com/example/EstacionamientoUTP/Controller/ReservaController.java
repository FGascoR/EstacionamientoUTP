package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.*;
import com.example.EstacionamientoUTP.Repository.*;
import com.example.EstacionamientoUTP.Security.CustomUserDetails;
import com.example.EstacionamientoUTP.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservas")
public class ReservaController {

    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final SectorRepository sectorRepository;
    private final EspacioRepository espacioRepository;
    private final SubEspacioRepository subEspacioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final EmailService emailService;

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
            @RequestParam("tipoVehiculoId") Long tipoVehiculoId,
            @RequestParam("fecha") String fecha,
            @RequestParam("entrada") String entrada,
            @RequestParam("salida") String salida) {

        LocalDate reqFecha = LocalDate.parse(fecha);
        LocalTime reqEntrada = LocalTime.parse(entrada);
        LocalTime reqSalida = LocalTime.parse(salida);

        List<Espacio> allSpaces = espacioRepository.findBySector_IdAndTipoVehiculo_Id(sectorId, tipoVehiculoId);
        List<Long> spaceIds = allSpaces.stream().map(Espacio::getId).toList();

        List<Reserva> spaceReservations = reservaRepository.findByFechaReservaAndEspacioIdIn(reqFecha, spaceIds);
        List<Reserva> subSpaceReservations = reservaRepository.findSubSpaceReservationsForDate(reqFecha);

        Map<Long, List<Reserva>> spaceResMap = spaceReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getEspacio().getId()));

        Map<Long, List<Reserva>> subSpaceResMap = subSpaceReservations.stream()
                .filter(r -> r.getSubEspacio() != null)
                .collect(Collectors.groupingBy(r -> r.getSubEspacio().getId()));

        for (Espacio space : allSpaces) {
            space.setEstado("LIBRE");
            if (spaceResMap.containsKey(space.getId())) {
                for (Reserva r : spaceResMap.get(space.getId())) {
                    if (r.getSubEspacio() == null && isConflict(reqEntrada, reqSalida, r)) {
                        space.setEstado("OCUPADO");
                        break;
                    }
                }
            }

            if (space.getSubEspacios() != null && !space.getSubEspacios().isEmpty()) {
                for (SubEspacio sub : space.getSubEspacios()) {
                    sub.setEstado("LIBRE");
                    if (subSpaceResMap.containsKey(sub.getId())) {
                        for (Reserva r : subSpaceResMap.get(sub.getId())) {
                            if (isConflict(reqEntrada, reqSalida, r)) {
                                sub.setEstado("OCUPADO");
                                break;
                            }
                        }
                    }
                }
            }
        }
        return allSpaces;
    }

    private boolean isConflict(LocalTime start, LocalTime end, Reserva existing) {
        return start.isBefore(existing.getHoraSalida()) && end.isAfter(existing.getHoraEntrada());
    }

    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardarReserva(
            @RequestParam Long sectorId,
            @RequestParam Long espacioId,
            @RequestParam(required = false) Long subEspacioId,
            @RequestParam String placa,
            @RequestParam String fecha,
            @RequestParam String entrada,
            @RequestParam String salida,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuarioEntity();

            LocalDate fechaRes = LocalDate.parse(fecha);
            long reservasDelDia = reservaRepository.countByUsuarioIdAndFechaReserva(usuario.getId(), fechaRes);

            if (reservasDelDia >= 2) {
                resp.put("success", false);
                resp.put("error", "limite_diario");
                return resp;
            }

            Espacio espacio = espacioRepository.findById(espacioId)
                    .orElseThrow(() -> new RuntimeException("Espacio no encontrado"));
            Sector sector = espacio.getSector();

            SubEspacio subEspacio = null;
            if (subEspacioId != null) {
                subEspacio = subEspacioRepository.findById(subEspacioId)
                        .orElseThrow(() -> new RuntimeException("SubEspacio no encontrado"));
            }

            LocalTime horaEnt = LocalTime.parse(entrada);
            LocalTime horaSal = LocalTime.parse(salida);

            List<Reserva> conflicts;
            if (subEspacio != null) {
                conflicts = reservaRepository.findConflictingSubSpaceReservations(subEspacioId, fechaRes, horaEnt, horaSal);
            } else {
                conflicts = reservaRepository.findConflictingReservations(espacioId, fechaRes, horaEnt, horaSal);
            }

            if (!conflicts.isEmpty()) {
                resp.put("success", false);
                resp.put("error", "ocupado");
                return resp;
            }

            String placaFinal = placa;
            if (placa == null || placa.trim().isEmpty()) {
                placaFinal = "SIN PLACA";
            }

            String finalPlaca = placaFinal;
            Vehiculo vehiculo = vehiculoRepository.findByPlaca(finalPlaca)
                    .orElseGet(() -> {
                        Vehiculo nuevo = Vehiculo.builder()
                                .placa(finalPlaca)
                                .tipoVehiculo(espacio.getTipoVehiculo())
                                .usuario(usuario)
                                .build();
                        return vehiculoRepository.save(nuevo);
                    });

            Reserva reserva = Reserva.builder()
                    .sector(sector)
                    .espacio(espacio)
                    .subEspacio(subEspacio)
                    .vehiculo(vehiculo)
                    .usuario(usuario)
                    .fechaReserva(fechaRes)
                    .horaEntrada(horaEnt)
                    .horaSalida(horaSal)
                    .estado("En Proceso")
                    .build();
            reservaRepository.save(reserva);

            new Thread(() -> emailService.enviarCorreoConfirmacion(usuario, reserva)).start();

            resp.put("success", true);
            resp.put("espacioId", espacioId);

            LocalDate hoy = LocalDate.now();
            LocalTime ahora = LocalTime.now();
            long totalCapacidad = calcularCapacidadTotal();
            long totalOcupados = reservaRepository.findActivasAhora(hoy, ahora).size();
            long totalDisponibles = Math.max(0, totalCapacidad - totalOcupados);

            resp.put("totales", Map.of("disponibles", totalDisponibles, "ocupados", totalOcupados));
            resp.put("sector", Map.of("id", sector.getId(), "disponibles", 0, "ocupados", 0));

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HH:mm");

            String nombreLugar = espacio.getNombre();
            if (subEspacio != null) {
                nombreLugar += " - " + subEspacio.getNombre();
            }

            Map<String, Object> nuevaReservaData = new HashMap<>();
            nuevaReservaData.put("id", reserva.getId());
            nuevaReservaData.put("fecha", reserva.getFechaReserva().format(dtfFecha));
            nuevaReservaData.put("entrada", reserva.getHoraEntrada().format(dtfHora));
            nuevaReservaData.put("salida", reserva.getHoraSalida().format(dtfHora));
            nuevaReservaData.put("sector", reserva.getSector().getNombre());
            nuevaReservaData.put("espacio", nombreLugar);
            nuevaReservaData.put("estado", "En Proceso");
            nuevaReservaData.put("placa", vehiculo.getPlaca());
            nuevaReservaData.put("tipo", vehiculo.getTipoVehiculo().getNombre());

            resp.put("nuevaReserva", nuevaReservaData);

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", e.getMessage());
        }
        return resp;
    }

    @PostMapping("/cancelar")
    @ResponseBody
    public Map<String, Object> cancelarReserva(@RequestParam Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            LocalDateTime fechaHoraEntrada = LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraEntrada());
            LocalDateTime ahora = LocalDateTime.now();

            long minutosParaEntrada = java.time.Duration.between(ahora, fechaHoraEntrada).toMinutes();

            if (minutosParaEntrada < 15) {
                resp.put("success", false);
                resp.put("error", "tiempo_limite");
                return resp;
            }

            Usuario usuarioReserva = reserva.getUsuario();
            new Thread(() -> emailService.enviarCorreoCancelacion(usuarioReserva, reserva)).start();

            reservaRepository.delete(reserva);

            LocalDate hoy = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            long totalCapacidad = calcularCapacidadTotal();
            long totalOcupados = reservaRepository.findActivasAhora(hoy, horaActual).size();

            resp.put("success", true);
            resp.put("totales", Map.of("disponibles", Math.max(0, totalCapacidad - totalOcupados), "ocupados", totalOcupados));

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getMessage());
        }
        return resp;
    }

    private long calcularCapacidadTotal() {
        long total = 0;
        List<Espacio> todos = espacioRepository.findAll();
        for(Espacio e : todos) {
            if(e.getSubEspacios() != null && !e.getSubEspacios().isEmpty()) {
                total += e.getSubEspacios().size();
            } else {
                total++;
            }
        }
        return total;
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