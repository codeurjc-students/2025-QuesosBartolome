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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
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

        Cheese curado = cheeseRepository.save(
                new Cheese(null, "Curado", 10.0, "desc1", "tipo1", "2024-01-01", "2025-01-01")
        );

        cheeseRepository.save(
                new Cheese(null, "Tierno", 12.0, "desc2", "tipo2", "2024-02-01", "2025-02-01")
        );

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

    @Test
    void shouldReturnEmptyImageWhenCheeseHasNoImage() {
        Cheese sinImagen = cheeseRepository.save(
                new Cheese(null, "SinImagen", 8.0, "desc3", "tipo3", "2024-03-01", "2025-03-01")
        );

        Optional<java.sql.Blob> image = cheeseService.getCheeseImageById(sinImagen.getId());
        assertThat(image).isEmpty();
    }

    @Test
    void shouldReturnImageWhenCheeseHasBlob() throws Exception {
        Cheese conImagen = new Cheese(null, "ConImagen", 15.0, "desc4", "tipo4", "2024-04-01", "2025-04-01");
        java.sql.Blob blob = new javax.sql.rowset.serial.SerialBlob("fakeimage".getBytes());
        conImagen.setImage(blob);

        Cheese saved = cheeseRepository.save(conImagen);

        Optional<java.sql.Blob> image = cheeseService.getCheeseImageById(saved.getId());
        assertThat(image).isPresent();
        assertThat(image.get().length()).isEqualTo(blob.length());
    }

    @Test
    void shouldCreateCheeseSuccessfully() {
        CheeseDTO dto = new CheeseDTO(
                null,
                "Nuevo",
                9.5,
                "desc",
                null,
                null,
                "tipoX",
                List.of(1.0, 2.0)
        );

        CheeseDTO created = cheeseService.createCheese(dto);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Nuevo");

        Optional<Cheese> saved = cheeseRepository.findById(created.id());
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Nuevo");
    }

    @Test
    void shouldFailWhenNameAlreadyExists() {
        CheeseDTO dto = new CheeseDTO(
                null,
                "Curado",
                9.5,
                "desc",
                null,
                null,
                "tipoX",
                List.of(1.0)
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                cheeseService.createCheese(dto)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cheese with that name already exists");
    }

    @Test
    void shouldFailWhenPriceInvalid() {
        CheeseDTO dto = new CheeseDTO(
                null,
                "Nuevo2",
                0.0,
                "desc",
                null,
                null,
                "tipoX",
                List.of(1.0)
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                cheeseService.createCheese(dto)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Price must be greater than 0");
    }

    @Test
    void shouldSetEmptyBoxesWhenNull() {
        CheeseDTO dto = new CheeseDTO(
                null,
                "Nuevo3",
                5.0,
                "desc",
                null,
                null,
                "tipoX",
                null
        );

        CheeseDTO created = cheeseService.createCheese(dto);

        Cheese saved = cheeseRepository.findById(created.id()).get();
        assertThat(saved.getBoxes()).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenCheeseNotFound() throws Exception {
        boolean result = cheeseService.saveCheeseImage(999L, null);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSaveDefaultImageWhenFileNull() throws Exception {
        Cheese cheese = cheeseRepository.save(
                new Cheese(null, "SinImg", 10.0, "d", "t", "2024-01-01", "2025-01-01")
        );

        boolean result = cheeseService.saveCheeseImage(cheese.getId(), null);

        assertThat(result).isTrue();

        Cheese updated = cheeseRepository.findById(cheese.getId()).get();
        assertThat(updated.getImage()).isNotNull();
    }

    @Test
    void shouldSaveDefaultImageWhenFileEmpty() throws Exception {
        Cheese cheese = cheeseRepository.save(
                new Cheese(null, "SinImg2", 10.0, "d", "t", "2024-01-01", "2025-01-01")
        );

        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        boolean result = cheeseService.saveCheeseImage(cheese.getId(), emptyFile);

        assertThat(result).isTrue();

        Cheese updated = cheeseRepository.findById(cheese.getId()).get();
        assertThat(updated.getImage()).isNotNull();
    }

    @Test
    void shouldSaveUploadedImage() throws Exception {
        Cheese cheese = cheeseRepository.save(
                new Cheese(null, "ConImg", 10.0, "d", "t", "2024-01-01", "2025-01-01")
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagen.png",
                "image/png",
                "fakeimage".getBytes()
        );

        boolean result = cheeseService.saveCheeseImage(cheese.getId(), file);

        assertThat(result).isTrue();

        Cheese updated = cheeseRepository.findById(cheese.getId()).get();
        assertThat(updated.getImage()).isNotNull();
        assertThat(updated.getImage().length()).isEqualTo("fakeimage".getBytes().length);
    }

    @Test
    void shouldUpdateCheeseSuccessfully() {
        Cheese original = cheeseRepository.save(
                new Cheese(null, "Original", 10.0, "desc", "tipo1", "2024-01-01", "2025-01-01")
        );

        CheeseDTO dto = new CheeseDTO(
                original.getId(),
                "Nuevo",
                20.0,
                "desc2",
                null,
                null,
                "tipoB",
                List.of(1.0, 2.0)
        );

        CheeseDTO updated = cheeseService.updateCheese(original.getId(), dto);

        assertThat(updated.name()).isEqualTo("Nuevo");
        assertThat(updated.price()).isEqualTo(20.0);

        Cheese saved = cheeseRepository.findById(original.getId()).get();
        assertThat(saved.getName()).isEqualTo("Nuevo");
        assertThat(saved.getPrice()).isEqualTo(20.0);
    }

    @Test
    void shouldFailUpdateWhenCheeseNotFound() {
        CheeseDTO dto = new CheeseDTO(
                999L,
                "Nuevo",
                20.0,
                "desc",
                null,
                null,
                "tipoB",
                List.of()
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                cheeseService.updateCheese(999L, dto)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cheese not found");
    }

    @Test
    void shouldFailUpdateWhenNameAlreadyExists() {
        Cheese c1 = cheeseRepository.save(
                new Cheese(null, "Original", 10.0, "desc", "tipo1", "2024-01-01", "2025-01-01")
        );

        cheeseRepository.save(
                new Cheese(null, "Duplicado", 12.0, "desc2", "tipo2", "2024-02-01", "2025-02-01")
        );

        CheeseDTO dto = new CheeseDTO(
                c1.getId(),
                "Duplicado",
                20.0,
                "desc",
                null,
                null,
                "tipoB",
                List.of()
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                cheeseService.updateCheese(c1.getId(), dto)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cheese with that name already exists");
    }

    @Test
    void shouldFailUpdateWhenPriceInvalid() {
        Cheese c1 = cheeseRepository.save(
                new Cheese(null, "Original", 10.0, "desc", "tipo1", "2024-01-01", "2025-01-01")
        );

        CheeseDTO dto = new CheeseDTO(
                c1.getId(),
                "Original",
                0.0,
                "desc",
                null,
                null,
                "tipo1",
                List.of()
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                cheeseService.updateCheese(c1.getId(), dto)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Price must be greater than 0");
    }

    @Test
    void shouldDeleteCheeseSuccessfully() {
        Cheese cheese = cheeseRepository.save(
                new Cheese(null, "Borrar", 10.0, "d", "t", "2024-01-01", "2025-01-01")
        );

        boolean result = cheeseService.deleteCheese(cheese.getId());

        assertThat(result).isTrue();
        assertThat(cheeseRepository.findById(cheese.getId())).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingCheese() {
        boolean result = cheeseService.deleteCheese(999L);
        assertThat(result).isFalse();
    }
}
