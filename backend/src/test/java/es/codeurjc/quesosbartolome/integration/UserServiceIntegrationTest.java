package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.dto.UserMapper;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.UserService;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.sql.Blob;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserCorrectly() {

        // Given
        User auxuser = new User(
                "john",
                "password123",
                "john@gmail.com",
                "Calle 1",
                "12345678A");

        UserDTO dto = userMapper.toDTO(auxuser);

        // When
        userService.createUser(dto);

        // Then
        Optional<User> user = userRepository.findByName("john");

        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("john");
        assertThat(user.get().getGmail()).isEqualTo("john@gmail.com");
        assertThat(user.get().getDirection()).isEqualTo("Calle 1");
        assertThat(user.get().getNif()).isEqualTo("12345678A");

        // The password should be encoded
        assertThat(passwordEncoder.matches("password123", user.get().getPassword())).isTrue();

    }

    @Test
    void shouldFindUserByName() {

        // Given
        User saved = new User("maria", passwordEncoder.encode("mypwd"), "maria@gmail.com", "Calle 2", "87654321B",
                "USER");

        userRepository.save(saved);

        // When
        Optional<UserDTO> user = userService.findByName("maria");

        // Then
        assertThat(user).isPresent();
        assertThat(user.get().name()).isEqualTo("maria");
        assertThat(user.get().gmail()).isEqualTo("maria@gmail.com");
        assertThat(user.get().direction()).isEqualTo("Calle 2");
        assertThat(user.get().nif()).isEqualTo("87654321B");
    }

    @Test
    void shouldFindUserById() {

        // Given
        User saved = new User(
                "carlos",
                passwordEncoder.encode("pwd123"),
                "carlos@gmail.com",
                "Calle 3",
                "99999999C",
                "USER");

        saved = userRepository.save(saved);

        // When
        Optional<UserDTO> result = userService.findUserById(saved.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("carlos");
        assertThat(result.get().gmail()).isEqualTo("carlos@gmail.com");
        assertThat(result.get().direction()).isEqualTo("Calle 3");
        assertThat(result.get().nif()).isEqualTo("99999999C");
    }

    @Test
    void shouldReturnEmptyWhenUserIdNotFound() {

        // When
        Optional<UserDTO> result = userService.findUserById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetUserImageById() throws Exception {

        // Given
        byte[] imgData = "fakeImageData".getBytes();
        Blob blob = new javax.sql.rowset.serial.SerialBlob(imgData);

        User user = new User(
                "ana",
                passwordEncoder.encode("pwd123"),
                "ana@gmail.com",
                "Calle Falsa 123",
                "12345678Z",
                "USER");

        user.setImage(blob);

        user = userRepository.save(user);

        // When
        Optional<Blob> result = userService.getUserImageById(user.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().length()).isEqualTo(blob.length());

        byte[] stored = result.get().getBytes(1, (int) result.get().length());
        assertThat(stored).isEqualTo(imgData);
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoImage() {

        // Given:
        User user = new User(
                "pepe",
                passwordEncoder.encode("pwd"),
                "pepe@gmail.com",
                "Calle X",
                "87654321Y",
                "USER");

        user = userRepository.save(user);

        // When
        Optional<Blob> result = userService.getUserImageById(user.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {

        // When
        Optional<Blob> result = userService.getUserImageById(9999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllUsersWithUserRole() {

        // Given
        User user1 = new User(
                "ana",
                passwordEncoder.encode("pwd1"),
                "ana@gmail.com",
                "Calle 1",
                "11111111A",
                "USER");

        User user2 = new User(
                "pedro",
                passwordEncoder.encode("pwd2"),
                "pedro@gmail.com",
                "Calle 2",
                "22222222B",
                "USER");

        User admin = new User(
                "admin",
                passwordEncoder.encode("adminpwd"),
                "admin@gmail.com",
                "Calle Admin",
                "99999999Z",
                "ADMIN");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(admin);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDTO> result = userService.findAllUsersWithUserRole(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent())
                .extracting(UserDTO::name)
                .containsExactlyInAnyOrder("ana", "pedro");
    }

    @Test
    void shouldPaginateUsersWithUserRole() {

        // Given
        for (int i = 1; i <= 5; i++) {
            User user = new User(
                    "user" + i,
                    passwordEncoder.encode("pwd"),
                    "user" + i + "@gmail.com",
                    "Calle " + i,
                    "0000000" + i + "A",
                    "USER");
            userRepository.save(user);
        }

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<UserDTO> result = userService.findAllUsersWithUserRole(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsersWithUserRole() {

        // Given
        User admin = new User(
                "admin",
                passwordEncoder.encode("pwd"),
                "admin@gmail.com",
                "Calle Admin",
                "99999999X",
                "ADMIN");

        userRepository.save(admin);

        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<UserDTO> result = userService.findAllUsersWithUserRole(pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

}
