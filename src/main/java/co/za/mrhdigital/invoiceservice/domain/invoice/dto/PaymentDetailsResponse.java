package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailsResponse {

    private String bank;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private String branchCode;
    private String reference;

    public static PaymentDetailsResponse from(PaymentDetails pd) {
        if (pd == null) return null;
        return PaymentDetailsResponse.builder()
                .bank(pd.getBank())
                .accountName(pd.getAccountName())
                .accountNumber(pd.getAccountNumber())
                .accountType(pd.getAccountType())
                .branchCode(pd.getBranchCode())
                .reference(pd.getReference())
                .build();
    }
}
