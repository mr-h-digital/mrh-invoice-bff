package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.LineItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItemResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private int sortOrder;

    public static LineItemResponse from(LineItem item) {
        return LineItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .sortOrder(item.getSortOrder())
                .build();
    }
}
