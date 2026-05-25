package co.za.mrhdigital.invoiceservice.domain.dashboard.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private BigDecimal totalOverdue;
    private long invoiceCount;
    private long paidCount;
    private long outstandingCount;
    private long overdueCount;
    private List<InvoiceResponse> recentInvoices;
}
