package es.codeurjc.quesosbartolome.repository;

import es.codeurjc.quesosbartolome.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	Optional<Invoice> findByOrderId(Long orderId);
	Page<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId, Pageable pageable);
	Optional<Invoice> findByIdAndUserId(Long id, Long userId);
}
