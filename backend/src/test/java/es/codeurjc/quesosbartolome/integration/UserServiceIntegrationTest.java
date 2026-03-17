package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.PasswordChangeDTO;
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
import org.springframework.mock.web.MockMultipartFile;

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
                User saved = new User("maria", passwordEncoder.encode("mypwd"), "maria@gmail.com", "Calle 2",
                                "87654321B",
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

        @Test
        void shouldReturnFalseWhenUserDoesNotExist_isAdmin() {

                boolean result = userService.isAdmin("ghost");

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenUserHasNoRoles_isAdmin() {

                User user = new User(
                                "pepe",
                                passwordEncoder.encode("pwd"),
                                "pepe@gmail.com",
                                "Calle X",
                                "11111111A");
                user.setRols(); // sin roles

                userRepository.save(user);

                boolean result = userService.isAdmin("pepe");

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenUserIsNotAdmin_isAdmin() {

                User user = new User(
                                "ana",
                                passwordEncoder.encode("pwd"),
                                "ana@gmail.com",
                                "Calle Y",
                                "22222222B",
                                "USER");

                userRepository.save(user);

                boolean result = userService.isAdmin("ana");

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrueWhenUserIsAdmin_isAdmin() {

                User user = new User(
                                "admin",
                                passwordEncoder.encode("pwd"),
                                "admin@gmail.com",
                                "Calle Admin",
                                "33333333C",
                                "USER", "ADMIN");

                userRepository.save(user);

                boolean result = userService.isAdmin("admin");

                assertThat(result).isTrue();
        }

        @Test
        void shouldUpdateUserSuccessfully() {

                // Given
                User user = new User(
                                "oldName",
                                passwordEncoder.encode("pwd"),
                                "old@gmail.com",
                                "Old Street",
                                "00000000A",
                                "USER");

                user = userRepository.save(user);

                UserDTO updateDTO = new UserDTO(
                                null, "newName",
                                "pwd",
                                "new@gmail.com",
                                "New Street",
                                "11111111B", null);

                // When
                Optional<UserDTO> result = userService.updateUser(user.getId(), updateDTO);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().name()).isEqualTo("newName");
                assertThat(result.get().gmail()).isEqualTo("new@gmail.com");
                assertThat(result.get().direction()).isEqualTo("New Street");
                assertThat(result.get().nif()).isEqualTo("11111111B");

                User updated = userRepository.findById(user.getId()).get();
                assertThat(updated.getName()).isEqualTo("newName");
        }

        @Test
        void shouldReturnEmptyWhenUpdatingNonExistingUser() {

                UserDTO dto = new UserDTO(null, "name", "pwd", "mail", "dir", "nif", null);

                Optional<UserDTO> result = userService.updateUser(999L, dto);

                assertThat(result).isEmpty();
        }

        @Test
        void shouldChangePasswordSuccessfully() {

                // Given
                User user = new User(
                                "juan",
                                passwordEncoder.encode("oldPassword"),
                                "juan@gmail.com",
                                "Calle X",
                                "12345678A",
                                "USER");

                user = userRepository.save(user);

                PasswordChangeDTO dto = new PasswordChangeDTO(
                                "oldPassword",
                                "newPassword123",
                                "newPassword123");

                // When
                boolean result = userService.changePassword(user.getId(), dto);

                // Then
                assertThat(result).isTrue();

                User updated = userRepository.findById(user.getId()).get();
                assertThat(passwordEncoder.matches("newPassword123", updated.getPassword())).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCurrentPasswordIsIncorrect_changePassword() {

                // Given
                User user = new User(
                                "luis",
                                passwordEncoder.encode("correctPwd"),
                                "luis@gmail.com",
                                "Calle Y",
                                "87654321B",
                                "USER");

                user = userRepository.save(user);

                PasswordChangeDTO dto = new PasswordChangeDTO(
                                "wrongPwd",
                                "newPassword123",
                                "newPassword123");

                // When
                boolean result = userService.changePassword(user.getId(), dto);

                // Then
                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNewPasswordsDoNotMatch_changePassword() {

                User user = new User(
                                "ana",
                                passwordEncoder.encode("pwd"),
                                "ana@gmail.com",
                                "Calle Z",
                                "11111111C",
                                "USER");

                user = userRepository.save(user);

                PasswordChangeDTO dto = new PasswordChangeDTO(
                                "pwd",
                                "new1",
                                "new2");

                boolean result = userService.changePassword(user.getId(), dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenUserDoesNotExist_changePassword() {

                PasswordChangeDTO dto = new PasswordChangeDTO(
                                "old",
                                "newPassword",
                                "newPassword");

                boolean result = userService.changePassword(999L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldUpdateUserImageSuccessfully() throws Exception {

                // Given
                User user = new User(
                                "sofia",
                                passwordEncoder.encode("pwd"),
                                "sofia@gmail.com",
                                "Calle Imagen",
                                "55555555A",
                                "USER");

                user = userRepository.save(user);

                byte[] img = "fakeImageData".getBytes();

                MockMultipartFile file = new MockMultipartFile(
                                "image",
                                "photo.jpg",
                                "image/jpeg",
                                img);

                // When
                boolean result = userService.updateUserImage(user.getId(), file);

                // Then
                assertThat(result).isTrue();

                User updated = userRepository.findById(user.getId()).get();
                assertThat(updated.getImage()).isNotNull();
                assertThat(updated.getImage().length()).isEqualTo(img.length);
        }

        @Test
        void shouldReturnFalseWhenUpdatingImageOfNonExistingUser() throws Exception {

                MockMultipartFile file = new MockMultipartFile(
                                "image",
                                "photo.jpg",
                                "image/jpeg",
                                "data".getBytes());

                boolean result = userService.updateUserImage(999L, file);

                assertThat(result).isFalse();
        }

}
