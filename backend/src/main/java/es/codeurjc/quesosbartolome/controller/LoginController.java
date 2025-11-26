package es.codeurjc.quesosbartolome.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.security.jwt.AuthResponse;
import es.codeurjc.quesosbartolome.security.jwt.AuthResponse.Status;
import es.codeurjc.quesosbartolome.security.jwt.LoginRequest;
import es.codeurjc.quesosbartolome.security.jwt.UserLoginService;
import es.codeurjc.quesosbartolome.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    @Autowired
	private UserService userService;

    @Autowired
	private UserLoginService userLoginService;

    @PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		
		return userLoginService.login(response, loginRequest);
	}

    @PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(
			@CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {

		return userLoginService.refresh(response, refreshToken);
	}

    @PostMapping("/logout")
	public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
	}

    
   @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {

        if (userDTO.name() == null || userDTO.name().isBlank() ||
            userDTO.gmail() == null || userDTO.gmail().isBlank() ||
            userDTO.password() == null || userDTO.password().isBlank() ||
            userDTO.direction() == null || userDTO.direction().isBlank() ||
            userDTO.nif() == null || userDTO.nif().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userDTO.password().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!userDTO.nif().matches("\\d{8}[A-Za-z]") || 
            !userDTO.gmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userService.findByName(userDTO.name()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserDTO createdUser = userService.createUser(userDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/v1/users/" + createdUser.id())
                .body(createdUser);
    }

    


}
    
