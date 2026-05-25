package co.za.mrhdigital.invoiceservice.domain.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    @Query("SELECT i FROM Invoice i LEFT JOIN i.client c WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.clientSnapshot.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> findAllFiltered(
            @Param("status") InvoiceStatus status,
            @Param("search") String search,
            Pageable pageable);

    List<Invoice> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    @Query("SELECT i.invoiceNumber FROM Invoice i " +
           "WHERE i.invoiceNumber LIKE CONCAT('INV-', :year, '-%') " +
           "ORDER BY i.invoiceNumber DESC")
    List<String> findInvoiceNumbersByYear(@Param("year") String year);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status IN ('SENT', 'OVERDUE')")
    BigDecimal sumOutstanding();

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal sumPaid();

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'OVERDUE'")
    BigDecimal sumOverdue();

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i")
    BigDecimal sumTotal();

    long countByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i ORDER BY i.createdAt DESC")
    List<Invoice> findRecent(Pageable pageable);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
