package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.dto.UserMapper;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Blob;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldCreateUserCorrectly() {

        // Given
        User auxuser = new User(
                "john",
                "password123",
                "john@gmail.com",
                "Calle 1",
                "12345678A"
        );

        UserDTO dto = userMapper.toDTO(auxuser);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        // When
        userService.createUser(dto);

        // Then:
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("john");
        assertThat(saved.getGmail()).isEqualTo("john@gmail.com");
        assertThat(saved.getDirection()).isEqualTo("Calle 1");
        assertThat(saved.getNif()).isEqualTo("12345678A");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void shouldFindUserByName() {

        // Given
        User saved = new User(
                "maria",
                "encodedPwd",
                "maria@gmail.com",
                "Calle 2",
                "87654321B"
        );

        when(userRepository.findByName("maria")).thenReturn(Optional.of(saved));

        // When
        Optional<UserDTO> result = userService.findByName("maria");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("maria");
        assertThat(result.get().gmail()).isEqualTo("maria@gmail.com");
        assertThat(result.get().direction()).isEqualTo("Calle 2");
        assertThat(result.get().nif()).isEqualTo("87654321B");
    }

    @Test
    void shouldReturnUserImageWhenExists() throws Exception {

        // Mock blob
        Blob blob = mock(Blob.class);

        User user = new User(
                "luis",
                "pwd",
                "luis@gmail.com",
                "Calle 3",
                "11223344C"
        );
        user.setId(5L);
        user.setImage(blob);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        // When
        Optional<Blob> result = userService.getUserImageById(5L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(blob);
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Blob> result = userService.getUserImageById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindUserById() {

        // Given
        User saved = new User(
                "carlos",
                "encodedPwd",
                "carlos@gmail.com",
                "Calle 3",
                "99999999C"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(saved));

        // When
        Optional<UserDTO> result = userService.findUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("carlos");
        assertThat(result.get().gmail()).isEqualTo("carlos@gmail.com");
        assertThat(result.get().direction()).isEqualTo("Calle 3");
        assertThat(result.get().nif()).isEqualTo("99999999C");
    }



}
