package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    List<Espacio> findBySector_IdAndTipoVehiculo_Id(Long sectorId, Long tipoVehiculoId);
}
