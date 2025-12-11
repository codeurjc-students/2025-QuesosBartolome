package es.codeurjc.quesosbartolome.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.dto.OrderMapper;
import es.codeurjc.quesosbartolome.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository; 
    
    @Autowired
    private OrderMapper orderMapper;

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toDTO); 
    }

    public OrderDTO getCurrentOrder(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentOrder'");
    }

    public OrderDTO addItemToCurrentOrder(Long userId, Long cheeseId, int boxes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addItemToCurrentOrder'");
    }
    
}
