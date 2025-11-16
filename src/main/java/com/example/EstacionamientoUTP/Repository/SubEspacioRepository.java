package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.SubEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubEspacioRepository extends JpaRepository<SubEspacio, Long> {

    List<SubEspacio> findByEspacioId(Long espacioId);

    List<SubEspacio> findByEstado(String estado);

    List<SubEspacio> findByEspacioIdAndEstado(Long espacioId, String estado);
}
