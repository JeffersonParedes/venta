package pe.edu.promart.sistemaventas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.promart.sistemaventas.entity.Usuario;
import pe.edu.promart.sistemaventas.repository.UsuarioRepository;

@SpringBootApplication
public class SistemaventasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaventasApplication.class, args);
    }

    // ESTE ES EL TRUCO:
    // Este código se ejecuta automáticamente cada vez que inicias la app.
    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Buscar al usuario admin
            Usuario admin = usuarioRepo.findByUsuario("admin").orElse(null);

            if (admin != null) {
                // 2. Imprimir qué contraseña tiene actualmente (para ver si Java la lee o sale
                // null)
                System.out.println(">>> CONTRASEÑA ACTUAL EN BD: " + admin.getPassword());

                // 3. CAMBIAR LA CONTRASEÑA A UNA NUEVA GENERADA AQUÍ
                String nuevaPass = passwordEncoder.encode("admin123");
                admin.setPassword(nuevaPass);
                usuarioRepo.save(admin);

                System.out.println(">>> ÉXITO: Contraseña de 'admin' actualizada a: " + nuevaPass);
                System.out.println(">>> PRUEBA INGRESAR AHORA CON: admin / admin123");
            } else {
                System.out.println(">>> ERROR: No se encontró al usuario 'admin' en la base de datos.");
            }
        };
    }
}
