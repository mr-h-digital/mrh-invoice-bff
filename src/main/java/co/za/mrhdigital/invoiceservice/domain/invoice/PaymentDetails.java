package co.za.mrhdigital.invoiceservice.domain.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetails {

    @Column(name = "payment_bank")
    private String bank;

    @Column(name = "payment_account_name")
    private String accountName;

    @Column(name = "payment_account_number")
    private String accountNumber;

    @Column(name = "payment_account_type")
    private String accountType;

    @Column(name = "payment_branch_code")
    private String branchCode;

    @Column(name = "payment_reference")
    private String reference;
}
