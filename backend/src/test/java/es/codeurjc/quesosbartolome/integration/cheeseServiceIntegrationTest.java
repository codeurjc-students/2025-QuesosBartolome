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

    private Long curadoId;

    @BeforeEach
    void setup() {
        cheeseRepository.deleteAll();
        Cheese curado = cheeseRepository.save(new Cheese(null, "Curado", 10.0, "desc1", "tipo1", "2024-01-01", "2025-01-01"));
        cheeseRepository.save(new Cheese(null, "Tierno", 12.0, "desc2", "tipo2", "2024-02-01", "2025-02-01"));
        curadoId = curado.getId(); 
    }

    @AfterAll
    static void afterAll(@Autowired CheeseRepository cheeseRepository) {
        cheeseRepository.deleteAll();
    }

    @Test
    void shouldReturnAllCheeses() {
        List<CheeseDTO> cheeses = cheeseService.findAll();
        assertThat(cheeses).hasSize(2);
        assertThat(cheeses).extracting(CheeseDTO::name).contains("Curado", "Tierno");
    }

    @Test
    void shouldReturnIdCheese() {
        Optional<CheeseDTO> cheese = cheeseService.findById(curadoId); 
        assertThat(cheese).isPresent();
        assertThat(cheese.get().name()).isEqualTo("Curado");
    }
}
