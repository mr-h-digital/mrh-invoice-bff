package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailsRequest {

    private String bank;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private String branchCode;
    private String reference;
}
