package es.codeurjc.quesosbartolome.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.OrderDTO;

import es.codeurjc.quesosbartolome.service.OrderService;

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

    @GetMapping("/user/{userId}/myorder")
    public ResponseEntity<OrderDTO> getCurrentOrder(@PathVariable Long userId) {
        try {
            OrderDTO dto = orderService.getCurrentOrder(userId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/{userId}/myorder")
    public ResponseEntity<OrderDTO> addItem(
            @PathVariable Long userId,
            @RequestParam Long cheeseId,
            @RequestParam int boxes) {

        try {
            OrderDTO dto = orderService.addItemToCurrentOrder(userId, cheeseId, boxes);
            return ResponseEntity.status(201).body(dto); // CREATED
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
}
