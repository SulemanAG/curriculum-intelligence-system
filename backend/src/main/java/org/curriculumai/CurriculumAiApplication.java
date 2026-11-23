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
        SpringApplication.run(CurriculumAiApplication.class, args);
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
