package co.za.mrhdigital.invoiceservice.domain.dashboard;

import co.za.mrhdigital.invoiceservice.domain.dashboard.dto.DashboardStatsResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceRepository;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceStatus;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final InvoiceRepository invoiceRepository;

    public DashboardStatsResponse getStats() {
        List<InvoiceResponse> recent = invoiceRepository
                .findRecent(PageRequest.of(0, 5))
                .stream()
                .map(InvoiceResponse::from)
                .toList();

        long outstandingCount = invoiceRepository.countByStatus(InvoiceStatus.SENT);
        long overdueCount = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);

        return DashboardStatsResponse.builder()
                .totalInvoiced(invoiceRepository.sumTotal())
                .totalPaid(invoiceRepository.sumPaid())
                .totalOutstanding(invoiceRepository.sumOutstanding())
                .totalOverdue(invoiceRepository.sumOverdue())
                .invoiceCount(invoiceRepository.count())
                .paidCount(invoiceRepository.countByStatus(InvoiceStatus.PAID))
                .outstandingCount(outstandingCount)
                .overdueCount(overdueCount)
                .recentInvoices(recent)
                .build();
    }
}
