package org.curriculumai;

import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class CurriculumAiApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(CurriculumAiApplication.class, args);
    }

    private static void loadDotEnvIfPresent() {
        java.io.File envFile = new java.io.File(".env");
        if (!envFile.exists()) {
            envFile = new java.io.File("backend/.env");
        }
        if (!envFile.exists()) {
            envFile = new java.io.File("../backend/.env");
        }
        if (envFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("#") && line.contains("=")) {
                        int eq = line.indexOf('=');
                        String key = line.substring(0, eq).trim();
                        String val = line.substring(eq + 1).trim();
                        // Local application .env takes precedence over host environment variables
                        System.setProperty(key, val);
                        if ("DB_URL".equals(key)) {
                            System.setProperty("spring.datasource.url", val);
                        } else if ("DB_USERNAME".equals(key)) {
                            System.setProperty("spring.datasource.username", val);
                        } else if ("DB_PASSWORD".equals(key)) {
                            System.setProperty("spring.datasource.password", val);
                        } else if ("GROQ_API_KEY".equals(key)) {
                            System.setProperty("app.llm.apiKey", val);
                        } else if ("GROQ_MODEL".equals(key)) {
                            System.setProperty("app.llm.model", val);
                        } else if ("GROQ_BASE_URL".equals(key)) {
                            System.setProperty("app.llm.baseUrl", val);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Bean
    CommandLineRunner seedDefaultFaculty(UserRepository userRepository) {
        return args -> {
            if (userRepository.findById("usr-default-faculty").isEmpty()) {
                User faculty = new User(
                        "usr-default-faculty",
                        "faculty_default",
                        "faculty@curriculumai.internal",
                        "Academic Faculty Member",
                        "FACULTY"
                );
                userRepository.save(faculty);
            }
        };
    }
}
