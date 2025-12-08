package pe.edu.promart.sistemaventas.controller.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        // Verificar si se ha seleccionado una caja
        if (session.getAttribute("SESSION_CAJA_ID") == null) {
            return "redirect:/auth/seleccionar-caja";
        }

        // Agregar datos de sesión al modelo
        model.addAttribute("cajaNombre", session.getAttribute("SESSION_CAJA_NOMBRE"));
        model.addAttribute("sucursalNombre", session.getAttribute("SESSION_SUCURSAL_NOMBRE"));

        return "home";
    }
}
