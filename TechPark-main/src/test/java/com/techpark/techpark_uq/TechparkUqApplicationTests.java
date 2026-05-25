package com.techpark.techpark_uq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
class TechparkUqApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("✅ Contexto de Spring cargado exitosamente!");
    }
}