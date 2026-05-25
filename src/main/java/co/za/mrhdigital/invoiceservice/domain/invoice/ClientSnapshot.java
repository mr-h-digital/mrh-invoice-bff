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
public class ClientSnapshot {

    @Column(name = "snapshot_company_name")
    private String companyName;

    @Column(name = "snapshot_contact_name")
    private String contactName;

    @Column(name = "snapshot_email")
    private String email;

    @Column(name = "snapshot_phone")
    private String phone;

    @Column(name = "snapshot_address", columnDefinition = "TEXT")
    private String address;
}
