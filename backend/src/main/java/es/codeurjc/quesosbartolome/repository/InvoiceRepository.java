package es.codeurjc.quesosbartolome.repository;

import es.codeurjc.quesosbartolome.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	java.util.Optional<Invoice> findByOrderId(Long orderId);
}
