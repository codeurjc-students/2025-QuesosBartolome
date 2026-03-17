package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.PasswordChangeDTO;
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
import org.springframework.web.multipart.MultipartFile;

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

        @Test
        void shouldUpdateUserCorrectly() {

                // Given
                User existing = new User(
                                "oldName",
                                "pwd",
                                "old@gmail.com",
                                "Old Street",
                                "00000000A");

                when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

                UserDTO updateDTO = new UserDTO(
                                null, "newName",
                                "pwd",
                                "new@gmail.com",
                                "New Street",
                                "11111111B", null);

                // When
                Optional<UserDTO> result = userService.updateUser(1L, updateDTO);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().name()).isEqualTo("newName");
                assertThat(result.get().gmail()).isEqualTo("new@gmail.com");
                assertThat(result.get().direction()).isEqualTo("New Street");
                assertThat(result.get().nif()).isEqualTo("11111111B");

                verify(userRepository).save(existing);
        }

        @Test
        void shouldReturnEmptyWhenUpdatingNonExistingUser() {

                when(userRepository.findById(99L)).thenReturn(Optional.empty());

                UserDTO dto = new UserDTO(null, "name", "pwd", "mail", "dir", "nif", null);

                Optional<UserDTO> result = userService.updateUser(99L, dto);

                assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnFalseWhenUserDoesNotExist_changePassword() {

                when(userRepository.findById(50L)).thenReturn(Optional.empty());

                PasswordChangeDTO dto = new PasswordChangeDTO("old", "newPassword", "newPassword");

                boolean result = userService.changePassword(50L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenDtoIsNull_changePassword() {

                when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

                boolean result = userService.changePassword(1L, null);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenFieldsAreNull_changePassword() {

                when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

                PasswordChangeDTO dto = new PasswordChangeDTO(null, null, null);

                boolean result = userService.changePassword(1L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNewPasswordsDoNotMatch_changePassword() {

                when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

                PasswordChangeDTO dto = new PasswordChangeDTO("old", "new1", "new2");

                boolean result = userService.changePassword(1L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNewPasswordTooShort_changePassword() {

                when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

                PasswordChangeDTO dto = new PasswordChangeDTO("old", "short", "short");

                boolean result = userService.changePassword(1L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenCurrentPasswordDoesNotMatch_changePassword() {

                User user = new User();
                user.setPassword("encodedOld");

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("wrongOld", "encodedOld")).thenReturn(false);

                PasswordChangeDTO dto = new PasswordChangeDTO("wrongOld", "newPassword", "newPassword");

                boolean result = userService.changePassword(1L, dto);

                assertThat(result).isFalse();
        }

        @Test
        void shouldChangePasswordSuccessfully() {

                User user = new User();
                user.setPassword("encodedOld");

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("oldPassword", "encodedOld")).thenReturn(true);
                when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");

                PasswordChangeDTO dto = new PasswordChangeDTO("oldPassword", "newPassword", "newPassword");

                boolean result = userService.changePassword(1L, dto);

                assertThat(result).isTrue();
                assertThat(user.getPassword()).isEqualTo("encodedNew");

                verify(userRepository).save(user);
        }

        @Test
        void shouldReturnFalseWhenUpdatingImageOfNonExistingUser() throws Exception {

                when(userRepository.findById(99L)).thenReturn(Optional.empty());

                MultipartFile file = mock(MultipartFile.class);

                boolean result = userService.updateUserImage(99L, file);

                assertThat(result).isFalse();
        }

        @Test
        void shouldUpdateUserImageSuccessfully() throws Exception {

                // Given
                User user = new User("juan", "pwd", "juan@gmail.com", "Calle X", "12345678A");
                user.setId(1L);

                MultipartFile file = mock(MultipartFile.class);

                byte[] imageBytes = "fakeImageData".getBytes();

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(imageBytes));
                when(file.getSize()).thenReturn((long) imageBytes.length);

                // When
                boolean result = userService.updateUserImage(1L, file);

                // Then
                assertThat(result).isTrue();
                assertThat(user.getImage()).isNotNull();

                verify(userRepository).save(user);
        }

        @Test
        void shouldStoreBlobWithCorrectSize_updateUserImage() throws Exception {

                // Given
                User user = new User("lola", "pwd", "lola@gmail.com", "Calle Y", "87654321B");
                user.setId(2L);

                MultipartFile file = mock(MultipartFile.class);

                byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };

                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(bytes));
                when(file.getSize()).thenReturn((long) bytes.length);

                // When
                boolean result = userService.updateUserImage(2L, file);

                // Then
                assertThat(result).isTrue();
                assertThat(user.getImage()).isNotNull();
                assertThat(user.getImage().length()).isEqualTo(bytes.length);

                verify(userRepository).save(user);
        }

}
