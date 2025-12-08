package pe.edu.promart.sistemaventas.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PosController {

    @GetMapping("/pos")
    public String puntoVenta() {
        // Simplemente retornamos la vista. 
        // La lógica de datos se maneja vía API REST (JavaScript -> VentaRestController)
        return "pos/punto_venta";
    }
}
