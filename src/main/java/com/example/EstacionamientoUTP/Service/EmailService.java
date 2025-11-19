package com.example.EstacionamientoUTP.Service;

import com.example.EstacionamientoUTP.Entity.Reserva;
import com.example.EstacionamientoUTP.Entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCorreoConfirmacion(Usuario usuario, Reserva reserva) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HH:mm");

            String detalleEspacio = reserva.getEspacio().getNombre();
            if (reserva.getSubEspacio() != null) {
                detalleEspacio += " - " + reserva.getSubEspacio().getNombre();
            }

            String textoCorreo = "¡Hola, " + usuario.getNombre() + "!\n\n" +
                    "Tu reserva de estacionamiento se ha registrado correctamente.\n\n" +
                    "Detalles de la reserva:\n" +
                    "- Fecha: " + reserva.getFechaReserva().format(dtfFecha) + "\n" +
                    "- Hora de Entrada: " + reserva.getHoraEntrada().format(dtfHora) + "\n" +
                    "- Hora de Salida: " + reserva.getHoraSalida().format(dtfHora) + "\n" +
                    "- Sector: " + reserva.getSector().getNombre() + "\n" +
                    "- Espacio: " + detalleEspacio + "\n\n" +
                    "Ten en cuenta tu hora de llegada y salida.\n" +
                    "¡Te esperamos!";

            message.setFrom(fromEmail);
            message.setTo(usuario.getCorreo());
            message.setSubject("Confirmación de Reserva - Estacionamiento UTP");
            message.setText(textoCorreo);

            mailSender.send(message);
            System.out.println("Correo de confirmación enviado a: " + usuario.getUsuario());

        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }
}