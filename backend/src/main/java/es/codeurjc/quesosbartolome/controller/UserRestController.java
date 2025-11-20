package es.codeurjc.quesosbartolome.controller;


import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<?> me(HttpServletRequest request) { 
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<UserDTO> user = userService.findByName(principal.getName());
            if (user.isPresent()) {
                UserDTO loggedInUser = user.get();
                return ResponseEntity.ok(loggedInUser);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be authenticated");
    }

    
}
