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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Blob;
import java.util.List;
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
                                "12345678A");

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
                                "87654321B");

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
                                "11223344C");
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
                                "99999999C");

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

        @Test
        void shouldFindAllUsersWithUserRole() {

                // Given
                Pageable pageable = PageRequest.of(0, 2);

                User user1 = new User(
                                "ana",
                                "pwd1",
                                "ana@gmail.com",
                                "Calle 4",
                                "11111111A");

                User user2 = new User(
                                "pedro",
                                "pwd2",
                                "pedro@gmail.com",
                                "Calle 5",
                                "22222222B");

                Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

                when(userRepository.findByRolsContaining("USER", pageable))
                                .thenReturn(userPage);

                // When
                Page<UserDTO> result = userService.findAllUsersWithUserRole(pageable);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(2);

                assertThat(result.getContent().get(0).name()).isEqualTo("ana");
                assertThat(result.getContent().get(1).name()).isEqualTo("pedro");

                verify(userRepository).findByRolsContaining("USER", pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoUsersWithUserRole() {

                // Given
                Pageable pageable = PageRequest.of(0, 5);
                Page<User> emptyPage = Page.empty(pageable);

                when(userRepository.findByRolsContaining("USER", pageable))
                                .thenReturn(emptyPage);

                // When
                Page<UserDTO> result = userService.findAllUsersWithUserRole(pageable);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEmpty();

                verify(userRepository).findByRolsContaining("USER", pageable);
        }

        @Test
        void shouldReturnFalseWhenUserDoesNotExist_isAdmin() {
                when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

                boolean result = userService.isAdmin("ghost");

                assertThat(result).isFalse();
                verify(userRepository).findByName("ghost");
        }

        @Test
        void shouldReturnFalseWhenUserHasNoRoles_isAdmin() {
                User user = new User("pepe", "pwd", "pepe@gmail.com", "Calle X", "11111111A");
                user.setRols();

                when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));

                boolean result = userService.isAdmin("pepe");

                assertThat(result).isFalse();
                verify(userRepository).findByName("pepe");
        }

        @Test
        void shouldReturnFalseWhenUserIsNotAdmin_isAdmin() {
                User user = new User("ana", "pwd", "ana@gmail.com", "Calle Y", "22222222B");
                user.setRols("USER");

                when(userRepository.findByName("ana")).thenReturn(Optional.of(user));

                boolean result = userService.isAdmin("ana");

                assertThat(result).isFalse();
                verify(userRepository).findByName("ana");
        }

        @Test
        void shouldReturnTrueWhenUserIsAdmin_isAdmin() {
                User user = new User("admin", "pwd", "admin@gmail.com", "Calle Z", "33333333C");
                user.setRols("USER", "ADMIN");

                when(userRepository.findByName("admin")).thenReturn(Optional.of(user));

                boolean result = userService.isAdmin("admin");

                assertThat(result).isTrue();
                verify(userRepository).findByName("admin");
        }

}
