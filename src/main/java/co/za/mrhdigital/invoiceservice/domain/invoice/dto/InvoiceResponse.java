package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.DiscountType;
import co.za.mrhdigital.invoiceservice.domain.invoice.Invoice;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private String id;
    private String invoiceNumber;
    private InvoiceStatus status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String clientId;
    private ClientSnapshotResponse clientSnapshot;
    private List<LineItemResponse> lineItems;
    private BigDecimal subtotal;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private boolean vatEnabled;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal total;
    private String notes;
    private PaymentDetailsResponse paymentDetails;
    private Instant createdAt;
    private Instant updatedAt;

    public static InvoiceResponse from(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .clientId(invoice.getClient() != null ? invoice.getClient().getId() : null)
                .clientSnapshot(ClientSnapshotResponse.from(invoice.getClientSnapshot()))
                .lineItems(invoice.getLineItems().stream().map(LineItemResponse::from).toList())
                .subtotal(invoice.getSubtotal())
                .discountType(invoice.getDiscountType())
                .discountValue(invoice.getDiscountValue())
                .discountAmount(invoice.getDiscountAmount())
                .vatEnabled(invoice.isVatEnabled())
                .vatRate(invoice.getVatRate())
                .vatAmount(invoice.getVatAmount())
                .total(invoice.getTotal())
                .notes(invoice.getNotes())
                .paymentDetails(PaymentDetailsResponse.from(invoice.getPaymentDetails()))
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
