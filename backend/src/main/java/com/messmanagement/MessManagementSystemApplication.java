// In: src/main/java/com/messmanagement/MessManagementSystemApplication.java
package com.messmanagement;

// Make sure you have all necessary imports
import com.messmanagement.user.entity.Role;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- IMPORTANT

@SpringBootApplication
public class MessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessManagementSystemApplication.class, args);
	}

	/**
	 * This CommandLineRunner bean will be executed automatically when the application starts.
	 * It checks if an admin user exists and creates one if not found.
	 * This is the recommended way to initialize data that requires application services like PasswordEncoder.
	 */
	@Bean
	CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Check if an admin user already exists to prevent creating duplicates
			if (!userRepository.existsByRole(Role.ADMIN)) {
				// Create the new admin user object
				User admin = new User();
				admin.setName("Default Admin");
				admin.setEmail("admin@example.com"); // Use your desired admin email

				// --- THIS IS THE FIX ---
				// 1. Encode the password using the PasswordEncoder bean
				// 2. Use the correct setPasswordHash() method
				admin.setPasswordHash(passwordEncoder.encode("YourSecureAdminPassword123!"));

				admin.setRole(Role.ADMIN);
				// Set other required non-nullable fields
				admin.setMobileNo("0000000000"); // Placeholder
				admin.setAddress("Admin Main Office"); // Placeholder

				// Save the admin user to the database
				userRepository.save(admin);
				System.out.println(">>> Default admin user created successfully!");
			} else {
				System.out.println(">>> Admin user already exists. Skipping creation.");
			}
		};
	}
}