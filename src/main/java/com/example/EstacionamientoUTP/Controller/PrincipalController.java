package com.example.EstacionamientoUTP.Controller;

import com.example.EstacionamientoUTP.Entity.*;
import com.example.EstacionamientoUTP.Repository.EspacioRepository;
import com.example.EstacionamientoUTP.Repository.SectorRepository;
import com.example.EstacionamientoUTP.Repository.ReservaRepository;
import com.example.EstacionamientoUTP.Repository.TipoVehiculoRepository;
import com.example.EstacionamientoUTP.Repository.VehiculoRepository;
import com.example.EstacionamientoUTP.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PrincipalController {

    private final SectorRepository sectorRepository;
    private final EspacioRepository espacioRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;

    @GetMapping("/UTPEstacionamiento")
    public String mostrarPrincipal(Model model, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuarioEntity();
        model.addAttribute("usuario", usuario);

        int totalDisponibles = 0;
        int totalOcupados = 0;

        int disponiblesA = 0, ocupadosA = 0;
        int disponiblesB = 0, ocupadosB = 0;
        int disponiblesC = 0, ocupadosC = 0;

        List<Sector> sectores = sectorRepository.findAll();

        for (Sector s : sectores) {
            totalDisponibles += s.getEspaciosDisponibles();
            totalOcupados += s.getEspaciosOcupados();

            switch (s.getNombre()) {
                case "A":
                    disponiblesA += s.getEspaciosDisponibles();
                    ocupadosA += s.getEspaciosOcupados();
                    break;
                case "B":
                    disponiblesB += s.getEspaciosDisponibles();
                    ocupadosB += s.getEspaciosOcupados();
                    break;
                case "C":
                    disponiblesC += s.getEspaciosDisponibles();
                    ocupadosC += s.getEspaciosOcupados();
                    break;
            }
        }

        model.addAttribute("disponibles", totalDisponibles);
        model.addAttribute("ocupados", totalOcupados);

        model.addAttribute("disponiblesA", disponiblesA);
        model.addAttribute("ocupadosA", ocupadosA);
        model.addAttribute("disponiblesB", disponiblesB);
        model.addAttribute("ocupadosB", ocupadosB);
        model.addAttribute("disponiblesC", disponiblesC);
        model.addAttribute("ocupadosC", ocupadosC);

        model.addAttribute("sectores", sectores);
        model.addAttribute("espacios", espacioRepository.findAll());
        model.addAttribute("tiposVehiculo", tipoVehiculoRepository.findAll());

        List<Vehiculo> vehiculosUsuario = vehiculoRepository.findByUsuarioId(usuario.getId());
        model.addAttribute("vehiculosUsuario", vehiculosUsuario);

        List<Reserva> misReservas = reservaRepository.findByUsuarioId(usuario.getId());

        if (!misReservas.isEmpty()) {
            model.addAttribute("reservaActiva", misReservas.get(0));
        } else {
            model.addAttribute("reservaActiva", null);
        }
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
