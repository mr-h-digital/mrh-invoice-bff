package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    private String invoiceNumber;

    private String clientId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @Valid
    @NotEmpty(message = "At least one line item is required")
    private List<LineItemRequest> lineItems;

    private DiscountType discountType;

    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Builder.Default
    private boolean vatEnabled = false;

    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("0.15");

    private String notes;

    @Valid
    private PaymentDetailsRequest paymentDetails;
}
