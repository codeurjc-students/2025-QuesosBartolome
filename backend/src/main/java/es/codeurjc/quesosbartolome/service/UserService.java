package es.codeurjc.quesosbartolome.service;

import java.sql.Blob;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

}
