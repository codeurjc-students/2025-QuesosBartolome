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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.quesosbartolome.dto.PasswordChangeDTO;
import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.security.jwt.JwtTokenProvider;
import es.codeurjc.quesosbartolome.security.jwt.TokenType;
import es.codeurjc.quesosbartolome.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

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

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Only the owner of the account can update it
        Optional<UserDTO> callerOpt = userService.findByName(principal.getName());
        if (callerOpt.isEmpty() || !callerOpt.get().id().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean nameChanged = dto.name() != null
                && !dto.name().isBlank()
                && !dto.name().equals(principal.getName());

        Optional<UserDTO> updated = userService.updateUser(id, dto);
        if (updated.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (nameChanged) {
            UserDetails newDetails = userDetailsService.loadUserByUsername(updated.get().name());
            Cookie accessCookie = buildTokenCookie(TokenType.ACCESS,
                    jwtTokenProvider.generateAccessToken(newDetails));
            Cookie refreshCookie = buildTokenCookie(TokenType.REFRESH,
                    jwtTokenProvider.generateRefreshToken(newDetails));
            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
        }

        return ResponseEntity.ok(updated.get());
    }

    private Cookie buildTokenCookie(TokenType type, String token) {
        Cookie cookie = new Cookie(type.cookieName, token);
        cookie.setMaxAge((int) type.duration.getSeconds());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Void> updateUserImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserDTO> callerOpt = userService.findByName(principal.getName());
        if (callerOpt.isEmpty() || !callerOpt.get().id().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean updated = userService.updateUserImage(id, file);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody PasswordChangeDTO dto,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserDTO> callerOpt = userService.findByName(principal.getName());
        if (callerOpt.isEmpty() || !callerOpt.get().id().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean updated = userService.changePassword(id, dto);
        if (!updated) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

}
