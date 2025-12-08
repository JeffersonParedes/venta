package pe.edu.promart.sistemaventas.controller.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.promart.sistemaventas.entity.Caja;
import pe.edu.promart.sistemaventas.entity.Usuario;
import pe.edu.promart.sistemaventas.repository.CajaRepository;
import pe.edu.promart.sistemaventas.repository.UsuarioRepository;

import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CajaRepository cajaRepository;

    // Muestra login.html
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Muestra seleccionar-caja.html
    @GetMapping("/seleccionar-caja")
    public String mostrarSeleccionCaja(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsuario(username).orElseThrow();
        Integer idSucursal = usuario.getEmpleado().getSucursal().getIdSucursal();
        String nombreSucursal = usuario.getEmpleado().getSucursal().getNombre();
        
        // Busca cajas activas
        List<Caja> cajas = cajaRepository.findBySucursalIdSucursalAndEstado(idSucursal, "activa");

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreSucursal", nombreSucursal);
        model.addAttribute("cajas", cajas);

        // IMPORTANTE: Este String debe ser idéntico al nombre de tu archivo HTML
        return "auth/seleccionar-caja"; 
    }

    // Procesa la selección y redirige al HOME
    @PostMapping("/seleccionar-caja")
    public String procesarSeleccion(@RequestParam Integer idCaja, HttpSession session) {
        Caja caja = cajaRepository.findById(idCaja).orElseThrow();
        
        // Guarda en sesión
        session.setAttribute("SESSION_CAJA_ID", caja.getIdCaja());
        session.setAttribute("SESSION_CAJA_NOMBRE", caja.getNombre());
        session.setAttribute("SESSION_SUCURSAL_ID", caja.getSucursal().getIdSucursal());
        session.setAttribute("SESSION_SUCURSAL_NOMBRE", caja.getSucursal().getNombre());

        // SALTO FINAL: Redirige a /home
        return "redirect:/home";
    }
}