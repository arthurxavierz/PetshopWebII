package com.petshop.petshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Usa o H2 em memoria, sem precisar do PostgreSQL rodando
class PetshopApplicationTests {

	@Test
	void contextLoads() {
	}

}
