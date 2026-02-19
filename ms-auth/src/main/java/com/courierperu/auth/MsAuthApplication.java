package com.courierperu.auth;

import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MsAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(AuthUserRepository repository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Crear ADMIN si no existe
			if (repository.findByUserName("admin_jamir").isEmpty()) {
				AuthUser admin = new AuthUser();
				admin.setUserName("admin_jamir");
				admin.setPassword(passwordEncoder.encode("123"));
				admin.setEmail("admin@courierperu.com");
				admin.setRole("ADMIN");
				repository.save(admin);
			}

			// Crear USER si no existe
			if (repository.findByUserName("user_demo").isEmpty()) {
				AuthUser user = new AuthUser();
				user.setUserName("user_demo");
				user.setPassword(passwordEncoder.encode("123"));
				user.setEmail("cliente@gmail.com");
				user.setRole("USER");
				repository.save(user);
			}

			System.out.println("✅ Usuarios de prueba inicializados correctamente.");
		};
	}
}