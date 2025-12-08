package pe.edu.promart.sistemaventas.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.promart.sistemaventas.entity.Usuario;
import pe.edu.promart.sistemaventas.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en la BD
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Recuperamos sus roles y los convertimos al formato de Spring
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Asumimos que la relación es usuario -> roles (Set<Rol>)
        if (usuario.getRoles() != null) {
            usuario.getRoles().forEach(rol -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase()));
            });
        }

        // 3. Retornamos el objeto User oficial de Spring Security
        return new User(
                usuario.getUsuario(), 
                usuario.getPassword(), // Aquí usa el hash de la BD
                usuario.getActivo(),   // Enabled
                true,                  // accountNonExpired
                true,                  // credentialsNonExpired
                true,                  // accountNonLocked
                authorities
        );
    }
}
