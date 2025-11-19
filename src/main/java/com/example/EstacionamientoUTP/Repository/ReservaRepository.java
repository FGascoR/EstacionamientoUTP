package com.example.EstacionamientoUTP.Repository;

import com.example.EstacionamientoUTP.Entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    @Query("SELECT r FROM Reserva r WHERE r.espacio.id = :espacioId " +
            "AND r.fechaReserva = :fecha " +
            "AND r.horaEntrada < :salida " +
            "AND r.horaSalida > :entrada " +
            "AND r.subEspacio IS NULL")
    List<Reserva> findConflictingReservations(
            @Param("espacioId") Long espacioId,
            @Param("fecha") LocalDate fecha,
            @Param("entrada") LocalTime entrada,
            @Param("salida") LocalTime salida
    );

    @Query("SELECT r FROM Reserva r WHERE r.subEspacio.id = :subEspacioId " +
            "AND r.fechaReserva = :fecha " +
            "AND r.horaEntrada < :salida " +
            "AND r.horaSalida > :entrada")
    List<Reserva> findConflictingSubSpaceReservations(
            @Param("subEspacioId") Long subEspacioId,
            @Param("fecha") LocalDate fecha,
            @Param("entrada") LocalTime entrada,
            @Param("salida") LocalTime salida
    );

    List<Reserva> findByFechaReservaAndEspacioIdIn(LocalDate fecha, List<Long> espacioIds);


    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva = :fecha AND r.subEspacio IS NOT NULL")
    List<Reserva> findSubSpaceReservationsForDate(@Param("fecha") LocalDate fecha);

    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva = :fecha " +
            "AND r.horaEntrada <= :horaActual " +
            "AND r.horaSalida > :horaActual")
    List<Reserva> findActivasAhora(
            @Param("fecha") LocalDate fecha,
            @Param("horaActual") LocalTime horaActual
    );
}