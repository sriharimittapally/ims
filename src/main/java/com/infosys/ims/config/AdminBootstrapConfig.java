package com.infosys.ims.config;

import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.Role;
import com.infosys.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class  AdminBootstrapConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.name}")
    private String adminName;

    @Bean
    public CommandLineRunner bootstrapAdmin() {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                Users admin = new Users();
                admin.setName(adminName);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setUserCode("ADM-0001");
                userRepository.save(admin);
                log.info("✅ Bootstrap admin created: {}", adminEmail);
            } else {
                log.info("ℹ️  Bootstrap admin already exists: {}", adminEmail);
            }
        };
    }
}