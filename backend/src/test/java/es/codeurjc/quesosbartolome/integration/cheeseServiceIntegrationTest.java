package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.service.CheeseService;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // usa application-test.properties
@Transactional 
public class cheeseServiceIntegrationTest {

    @Autowired
    private CheeseService cheeseService;

    @Autowired
    private CheeseRepository cheeseRepository;


    @BeforeEach
    void setup() {

        cheeseRepository.save(new Cheese(1L, "Curado", 10.0, "desc1", "tipo1", "2024-01-01", "2025-01-01"));
        cheeseRepository.save(new Cheese(2L, "Tierno", 12.0, "desc2", "tipo2", "2024-02-01", "2025-02-01"));
    }
    
    @AfterAll
    static void afterAll(@Autowired CheeseRepository cheeseRepository) {
        cheeseRepository.deleteAll();
    }



    @Test
    void shouldReturnAllCheeses() {
        // Ejecuta el servicio real
        List<CheeseDTO> cheeses = cheeseService.findAll();

        // Verifica los resultados
        assertThat(cheeses).hasSize(2);
        assertThat(cheeses).extracting(CheeseDTO::name).contains("Curado", "Tierno");
    }

    @Test
    void shouldReturnIdCheese() {
        // Ejecuta el servicio real
        Optional<CheeseDTO> cheese = cheeseService.findById(1L);

        // Verifica los resultados
        assertThat(cheese).isPresent();
        assertThat(cheese.get().name()).isEqualTo("Curado");
    }
}
