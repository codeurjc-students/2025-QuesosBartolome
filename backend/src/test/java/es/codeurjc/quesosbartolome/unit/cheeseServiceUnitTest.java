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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void getCheeseImageByIdReturnsEmptyWhenCheeseNotFound() {
        // GIVEN
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN
        Optional<java.sql.Blob> result = cheeseService.getCheeseImageById(99L);

        // THEN
        assertThat(result).isEmpty();
        verify(cheeseRepository, times(1)).findById(99L);
    }

    @Test
    void getCheeseImageByIdReturnsEmptyWhenImageIsNull() {
        // GIVEN
        Cheese c1 = new Cheese(1L, "SinImagen", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        c1.setImage(null);
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(c1));

        // WHEN
        Optional<java.sql.Blob> result = cheeseService.getCheeseImageById(1L);

        // THEN
        assertThat(result).isEmpty();
        verify(cheeseRepository, times(1)).findById(1L);
    }

    @Test
    void getCheeseImageByIdReturnsBlobWhenPresent() throws Exception {
        // GIVEN
        Cheese c1 = new Cheese(1L, "ConImagen", 10.0, "desc", "type", "2024-01-01", "2025-01-01");

        // Creating a fake Blob for testing
        java.sql.Blob blob = new javax.sql.rowset.serial.SerialBlob("fakeimage".getBytes());
        c1.setImage(blob);

        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(c1));

        // WHEN
        Optional<java.sql.Blob> result = cheeseService.getCheeseImageById(1L);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().length()).isEqualTo(blob.length());
        verify(cheeseRepository, times(1)).findById(1L);
    }

    @Test
    void createCheeseThrowsWhenNameExists() {
        // GIVEN
        CheeseDTO dto = new CheeseDTO(
                null, "Semicurado", 10.0, "desc",
                null, null, "type", List.of(1.0));

        when(cheeseRepository.existsByName("Semicurado")).thenReturn(true);

        // WHEN + THEN
        assertThatThrownBy(() -> cheeseService.createCheese(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cheese with that name already exists");

        verify(cheeseRepository).existsByName("Semicurado");
        verify(cheeseRepository, never()).save(any());
    }

    @Test
    void createCheeseThrowsWhenPriceInvalid() {
        // GIVEN
        CheeseDTO dto = new CheeseDTO(
                null, "Nuevo", 0.0, "desc",
                null, null, "type", List.of(1.0));

        when(cheeseRepository.existsByName("Nuevo")).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> cheeseService.createCheese(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must be greater than 0");

        verify(cheeseRepository).existsByName("Nuevo");
        verify(cheeseRepository, never()).save(any());
    }

    @Test
    void createCheeseSetsEmptyBoxesWhenNull() {
        // GIVEN
        CheeseDTO dto = new CheeseDTO(
                null, "Nuevo", 10.0, "desc",
                null, null, "type", null);

        when(cheeseRepository.existsByName("Nuevo")).thenReturn(false);

        Cheese saved = new Cheese(1L, "Nuevo", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        saved.setBoxes(List.of());
        saved.setImage(null);

        when(cheeseRepository.save(any())).thenReturn(saved);

        // WHEN
        CheeseDTO result = cheeseService.createCheese(dto);

        // THEN
        assertThat(result.name()).isEqualTo("Nuevo");
        verify(cheeseRepository).save(any(Cheese.class));
    }

    @Test
    void createCheeseSavesCorrectly() {
        // GIVEN
        CheeseDTO dto = new CheeseDTO(
                null, "Nuevo", 15.0, "desc",
                null, null, "type", List.of(1.0, 2.0));

        when(cheeseRepository.existsByName("Nuevo")).thenReturn(false);

        Cheese saved = new Cheese(1L, "Nuevo", 15.0, "desc", "type", "2024-01-01", "2025-01-01");
        saved.setBoxes(List.of(1.0, 2.0));
        saved.setImage(null);

        when(cheeseRepository.save(any())).thenReturn(saved);

        // WHEN
        CheeseDTO result = cheeseService.createCheese(dto);

        // THEN
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Nuevo");
        verify(cheeseRepository).save(any(Cheese.class));
    }

    @Test
    void saveCheeseImageReturnsFalseWhenCheeseNotFound() throws Exception {
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = cheeseService.saveCheeseImage(99L, null);

        assertThat(result).isFalse();
        verify(cheeseRepository).findById(99L);
    }

    @Test
    void saveCheeseImageUsesDefaultWhenFileNull() throws Exception {
        Cheese cheese = new Cheese(1L, "Queso", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        boolean result = cheeseService.saveCheeseImage(1L, null);

        assertThat(result).isTrue();
        assertThat(cheese.getImage()).isNotNull();
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void saveCheeseImageUsesDefaultWhenFileEmpty() throws Exception {
        Cheese cheese = new Cheese(1L, "Queso", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        boolean result = cheeseService.saveCheeseImage(1L, file);

        assertThat(result).isTrue();
        assertThat(cheese.getImage()).isNotNull();
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void saveCheeseImageStoresUploadedFile() throws Exception {
        Cheese cheese = new Cheese(1L, "Queso", 10.0, "desc", "type", "2024-01-01", "2025-01-01");
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("fakeimage".getBytes());

        boolean result = cheeseService.saveCheeseImage(1L, file);

        assertThat(result).isTrue();
        assertThat(cheese.getImage()).isNotNull();
        verify(cheeseRepository).save(cheese);
    }

}
