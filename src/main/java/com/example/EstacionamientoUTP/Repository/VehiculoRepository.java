package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByUsuarioId(Long usuarioId);
    Optional<Vehiculo> findByPlaca(String placa);
    List<Vehiculo> findByUsuarioIdAndTipoVehiculoId(Long usuarioId, Long tipoVehiculoId);
}
