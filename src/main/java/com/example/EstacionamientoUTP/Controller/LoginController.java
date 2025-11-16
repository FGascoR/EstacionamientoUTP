package com.example.EstacionamientoUTP.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/Login")
public class LoginController {

    @GetMapping
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
        }

        if (logout != null) {
            model.addAttribute("error", "Sesión cerrada correctamente.");
        }

        return "Login";
    }
}
