package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {

    Optional<TipoVehiculo> findByNombre(String nombre);

    Optional<TipoVehiculo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);

    List<TipoVehiculo> findByNombreIn(List<String> nombres);
}
