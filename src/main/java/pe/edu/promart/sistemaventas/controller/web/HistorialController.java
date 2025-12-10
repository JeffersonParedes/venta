package pe.edu.promart.sistemaventas.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ventas")
public class HistorialController {

    @GetMapping("/historial")
    public String verHistorial() {
        return "ventas/historial";
    }
}
