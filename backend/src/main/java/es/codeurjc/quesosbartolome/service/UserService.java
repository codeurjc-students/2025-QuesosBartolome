package es.codeurjc.quesosbartolome.service;

import java.io.InputStream;
import java.sql.Blob;
import java.util.Optional;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.quesosbartolome.dto.PasswordChangeDTO;
import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.dto.UserMapper;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<UserDTO> findByName(String name) {

        Optional<User> user = repository.findByName(name);
        if (user.isPresent()) {
            return Optional.of(mapper.toDTO(user.get()));
        } else {
            return Optional.empty();
        }
    }

    public UserDTO createUser(UserDTO userDTO) {
        User user = new User(
                userDTO.name(),
                passwordEncoder.encode(userDTO.password()),
                userDTO.gmail(),
                userDTO.direction(),
                userDTO.nif(),
                "USER");
        user.setBanned(false);

        // Set default profile image
        try {
            InputStream defaultImageStream = getClass().getResourceAsStream("/images/default-profile.jpg");
            if (defaultImageStream != null) {
                byte[] defaultBytes = defaultImageStream.readAllBytes();
                Blob defaultBlob = BlobProxy.generateProxy(defaultBytes);
                user.setImage(defaultBlob);
            }
        } catch (Exception e) {
            // If default image fails, continue without image
        }

        repository.save(user);

        return mapper.toDTO(user);
    }

    public Optional<UserDTO> findUserById(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            return Optional.of(mapper.toDTO(user.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Blob> getUserImageById(Long id) {
        Optional<User> userOpt = repository.findById(id);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(userOpt.get().getImage());
    }

    public Page<UserDTO> findAllUsersWithUserRole(Pageable pageable) {
        return repository.findByRolsContaining("USER", pageable)
                .map(user -> mapper.toDTO(user));
    }

    public Optional<UserDTO> updateUser(Long id, UserDTO dto) {
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        if (dto.gmail() != null) {
            user.setGmail(dto.gmail());
        }
        if (dto.direction() != null) {
            user.setDirection(dto.direction());
        }
        if (dto.nif() != null) {
            user.setNif(dto.nif());
        }

        repository.save(user);
        return Optional.of(mapper.toDTO(user));
    }

    public boolean changePassword(Long id, PasswordChangeDTO dto) {
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isEmpty() || dto == null) {
            return false;
        }

        if (dto.currentPassword() == null || dto.newPassword() == null || dto.confirmPassword() == null) {
            return false;
        }

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            return false;
        }

        if (dto.newPassword().length() < 8) {
            return false;
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        repository.save(user);
        return true;
    }

    public boolean updateUserImage(Long id, MultipartFile file) throws Exception {
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Blob blob = BlobProxy.generateProxy(file.getInputStream(), file.getSize());
        user.setImage(blob);
        repository.save(user);
        return true;
    }

    public boolean isAdmin(String username) {

        Optional<User> userOpt = repository.findByName(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return user.getRols() != null && user.getRols().contains("ADMIN");
    }

    public Optional<UserDTO> toggleUserBan(Long id) {
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        user.setBanned(!user.isBanned());
        repository.save(user);
        return Optional.of(mapper.toDTO(user));
    }

}
