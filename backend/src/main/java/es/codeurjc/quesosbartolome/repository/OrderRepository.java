package es.codeurjc.quesosbartolome.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.quesosbartolome.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


}
