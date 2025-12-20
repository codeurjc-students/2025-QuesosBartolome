package es.codeurjc.quesosbartolome.controller;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    // GET /api/v1/orders?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<User> userOpt = orderService.findOwnerByName(principal.getName());
        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            OrderDTO dto = orderService.confirmOrder(userOpt.get().getId());

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(dto.id())
                    .toUri();

            return ResponseEntity.created(location).body(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
