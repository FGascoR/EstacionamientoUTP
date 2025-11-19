package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.*;
import com.example.EstacionamientoUTP.Repository.*;
import com.example.EstacionamientoUTP.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PrincipalController {

    private final SectorRepository sectorRepository;
    private final EspacioRepository espacioRepository;
    private final SubEspacioRepository subEspacioRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;

    @GetMapping("/UTPEstacionamiento")
    public String mostrarPrincipal(Model model, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuarioEntity();
        model.addAttribute("usuario", usuario);

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Espacio> todosEspacios = espacioRepository.findAll();
        List<SubEspacio> todosSubEspacios = subEspacioRepository.findAll();

        Set<Long> idsEspaciosConHijos = todosSubEspacios.stream()
                .map(s -> s.getEspacio().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> capacidadPorSector = new HashMap<>();

        for (Espacio e : todosEspacios) {
            if (!idsEspaciosConHijos.contains(e.getId())) {
                Long sectorId = e.getSector().getId();
                capacidadPorSector.put(sectorId, capacidadPorSector.getOrDefault(sectorId, 0L) + 1);
            }
        }

        for (SubEspacio s : todosSubEspacios) {
            Long sectorId = s.getEspacio().getSector().getId();
            capacidadPorSector.put(sectorId, capacidadPorSector.getOrDefault(sectorId, 0L) + 1);
        }

        List<Reserva> reservasActivasAhora = reservaRepository.findActivasAhora(hoy, ahora);

        Map<Long, Long> ocupadosPorSector = reservasActivasAhora.stream()
                .collect(Collectors.groupingBy(r -> r.getSector().getId(), Collectors.counting()));

        long totalCapacidadGlobal = capacidadPorSector.values().stream().mapToLong(Long::longValue).sum();
        long totalOcupadosGlobal = reservasActivasAhora.size();
        long totalDisponiblesGlobal = Math.max(0, totalCapacidadGlobal - totalOcupadosGlobal);

        model.addAttribute("disponibles", totalDisponiblesGlobal);
        model.addAttribute("ocupados", totalOcupadosGlobal);

        List<Sector> sectores = sectorRepository.findAll();
        for (Sector s : sectores) {
            long capacidad = capacidadPorSector.getOrDefault(s.getId(), 0L);
            long ocupados = ocupadosPorSector.getOrDefault(s.getId(), 0L);
            long disponibles = Math.max(0, capacidad - ocupados);

            s.setEspaciosOcupados((int) ocupados);
            s.setEspaciosDisponibles((int) disponibles);
        }

        model.addAttribute("sectores", sectores);
        model.addAttribute("tiposVehiculo", tipoVehiculoRepository.findAll());
        List<Vehiculo> vehiculosUsuario = vehiculoRepository.findByUsuarioId(usuario.getId());
        model.addAttribute("vehiculosUsuario", vehiculosUsuario);

        List<Reserva> misReservas = reservaRepository.findByUsuarioId(usuario.getId());
        model.addAttribute("reservaActiva", misReservas.isEmpty() ? null : misReservas.get(0));

        return "Principal";
    }

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/vehiculos")
    public class VehiculoController {

        private final VehiculoRepository vehiculoRepository;
        private final TipoVehiculoRepository tipoVehiculoRepository;

        @GetMapping("/mios")
        public List<VehiculoDTO> misVehiculos(Authentication auth) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long usuarioId = userDetails.getUsuarioEntity().getId();

            return vehiculoRepository.findByUsuarioId(usuarioId)
                    .stream()
                    .map(v -> new VehiculoDTO(v.getId(), v.getPlaca(), v.getTipoVehiculo().getNombre()))
                    .toList();
        }

        @PostMapping("/agregar")
        public Map<String, Object> agregarVehiculo(@RequestBody Map<String, String> payload, Authentication auth) {
            Map<String, Object> resp = new HashMap<>();
            try {
                CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                var usuario = userDetails.getUsuarioEntity();

                Long tipoId = Long.parseLong(payload.get("tipoId"));
                String placa = payload.get("placa");

                var tipoVehiculo = tipoVehiculoRepository.findById(tipoId)
                        .orElseThrow(() -> new RuntimeException("Tipo de vehículo no encontrado"));

                if(vehiculoRepository.findByPlaca(placa).isPresent()){
                    resp.put("success", false);
                    resp.put("error", "Placa ya existe");
                    return resp;
                }

                Vehiculo nuevo = Vehiculo.builder()
                        .placa(placa)
                        .tipoVehiculo(tipoVehiculo)
                        .usuario(usuario)
                        .build();
                vehiculoRepository.save(nuevo);

                resp.put("success", true);
            } catch(Exception e){
                resp.put("success", false);
                resp.put("error", e.getMessage());
            }
            return resp;
        }

        @DeleteMapping("/eliminar/{id}")
        public Map<String, Object> eliminarVehiculo(@PathVariable Long id) {
            Map<String, Object> resp = new HashMap<>();
            try{
                vehiculoRepository.deleteById(id);
                resp.put("success", true);
            } catch(Exception e){
                resp.put("success", false);
                resp.put("error", e.getMessage());
            }
            return resp;
        }
    }
}