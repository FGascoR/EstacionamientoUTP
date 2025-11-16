package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}
