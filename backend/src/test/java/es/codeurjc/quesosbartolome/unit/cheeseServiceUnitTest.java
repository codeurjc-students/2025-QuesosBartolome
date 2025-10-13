package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.dto.CheeseMapper;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.service.CheeseService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class cheeseServiceUnitTest {

    @Mock
    private CheeseRepository cheeseRepository;

    @Spy
    private CheeseMapper cheeseMapper = Mappers.getMapper(CheeseMapper.class);

    @InjectMocks
    private CheeseService cheeseService;

    @Test
    void findAllReturnsExampleCheeses() {
        // GIVEN
        Cheese c1 = new Cheese(1L, "Semicurado", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        Cheese c2 = new Cheese(2L, "Azul", 12.0, "desc2", "typeB", "2024-02-01", "2025-02-01");

        when(cheeseRepository.findAll()).thenReturn(List.of(c1, c2));

        // WHEN
        List<CheeseDTO> result = cheeseService.findAll();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CheeseDTO::name)
                .contains("Semicurado", "Azul");

        verify(cheeseRepository, times(1)).findAll();
    }

    @Test
    void findByIdReturnsOptional() {
        // GIVEN
        Cheese c1 = new Cheese(1L, "Semicurado", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(c1));

        // WHEN
        Optional<CheeseDTO> maybe = cheeseService.findById(1L);

        // THEN
        assertThat(maybe).isPresent();
        assertThat(maybe.get().name()).isEqualTo("Semicurado");

        verify(cheeseRepository, times(1)).findById(1L);
    }
}

