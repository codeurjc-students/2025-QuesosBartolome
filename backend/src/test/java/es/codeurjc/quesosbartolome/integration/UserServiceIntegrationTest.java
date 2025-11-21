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

import java.util.Optional;

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

        //Given
        User auxuser = new User(
                "john",
                "password123",
                "john@gmail.com",
                "Calle 1",
                "12345678A"
        );

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
        User saved =new User("maria", passwordEncoder.encode("mypwd"), "maria@gmail.com", "Calle 2", "87654321B", "USER");

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
}
