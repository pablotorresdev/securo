package com.mb.conitrack.service.maestro;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole().getName()) // Extraer el nombre del rol
            .build();
    }

    @PostConstruct
    public void saveDefaultUsers() {
        log.info("Inicializando roles y usuarios por defecto...");

        // Crear/actualizar todos los 8 roles con sus niveles
        Role adminRole = createOrUpdateRole(RoleEnum.ADMIN);
        Role dtRole = createOrUpdateRole(RoleEnum.DT);
        Role gerenteGarantiaRole = createOrUpdateRole(RoleEnum.GERENTE_GARANTIA_CALIDAD);
        Role gerenteControlRole = createOrUpdateRole(RoleEnum.GERENTE_CONTROL_CALIDAD);
        Role supervisorPlantaRole = createOrUpdateRole(RoleEnum.SUPERVISOR_PLANTA);
        Role analistaControlRole = createOrUpdateRole(RoleEnum.ANALISTA_CONTROL_CALIDAD);
        Role analistaPlantaRole = createOrUpdateRole(RoleEnum.ANALISTA_PLANTA);
        Role auditorRole = createOrUpdateRole(RoleEnum.AUDITOR);

        log.info("Roles creados/actualizados: 8 roles con jerarquía definida");

        // Usuarios del sistema (password = username)
        createUserIfNotExists("admin", "admin", adminRole);                    // ADMIN
        createUserIfNotExists("ptorres", "ptorres", adminRole);                // ADMIN
        createUserIfNotExists("jtorres", "jtorres", dtRole);                   // DT
        createUserIfNotExists("cponce", "cponce", gerenteGarantiaRole);        // GERENTE_GARANTIA_CALIDAD
        createUserIfNotExists("jmartinez", "jmartinez", gerenteControlRole);   // GERENTE_CONTROL_CALIDAD
        createUserIfNotExists("rgonzalez", "rgonzalez", analistaControlRole);  // ANALISTA_CONTROL_CALIDAD
        createUserIfNotExists("msilva", "msilva", supervisorPlantaRole);       // SUPERVISOR_PLANTA
        createUserIfNotExists("eraciti", "eraciti", analistaPlantaRole);       // ANALISTA_PLANTA
        createUserIfNotExists("auditor", "auditor", auditorRole);              // AUDITOR

        log.info("Usuarios por defecto creados correctamente");
    }

    /**
     * Crea o actualiza un rol desde el enum RoleEnum.
     * Si el rol existe pero tiene nivel diferente, lo actualiza.
     */
    private Role createOrUpdateRole(RoleEnum roleEnum) {
        return roleRepository.findByName(roleEnum.name())
            .map(existingRole -> {
                // Actualizar nivel si cambió
                if (!existingRole.getNivel().equals(roleEnum.getNivel())) {
                    existingRole.setNivel(roleEnum.getNivel());
                    Role updated = roleRepository.save(existingRole);
                    log.debug("Rol {} actualizado con nivel {}", roleEnum.name(), roleEnum.getNivel());
                    return updated;
                }
                return existingRole;
            })
            .orElseGet(() -> {
                Role newRole = Role.fromEnum(roleEnum);
                Role saved = roleRepository.save(newRole);
                log.info("Rol {} creado con nivel {}", roleEnum.name(), roleEnum.getNivel());
                return saved;
            });
    }

    /**
     * Crea un usuario si no existe.
     */
    private void createUserIfNotExists(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new User(username, passwordEncoder().encode(password), role));
            log.info("Usuario {} creado con rol {}", username, role.getName());
        }
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use a private static encoder here
    }

}
