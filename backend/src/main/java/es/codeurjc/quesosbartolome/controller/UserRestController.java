package es.codeurjc.quesosbartolome.controller;

import java.security.Principal;
import java.sql.Blob;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<UserDTO> me(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }

        Optional<UserDTO> user = userService.findByName(principal.getName());

        return user
                .map(ResponseEntity::ok) // 200 + UserDTO
                .orElseGet(() -> ResponseEntity // 404
                        .status(HttpStatus.NOT_FOUND)
                        .build());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) throws Exception {

        Optional<UserDTO> userOptional = userService.findUserById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
        }

        Optional<Blob> imageOpt = userService.getUserImageById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Blob blob = imageOpt.get();

        byte[] bytes = blob.getBytes(1, (int) blob.length());

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(bytes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<UserDTO> userOpt = userService.findUserById(id);

        return userOpt
                .map(ResponseEntity::ok) // 200 + UserDTO
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserDTO>> getUsersWithUserRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.findAllUsersWithUserRole(pageable);

        return ResponseEntity.ok(users);
    }

}
