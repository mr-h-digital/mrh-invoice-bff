package co.za.mrhdigital.invoiceservice.domain.invoice.dto;

import co.za.mrhdigital.invoiceservice.domain.invoice.ClientSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSnapshotResponse {

    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String address;

    public static ClientSnapshotResponse from(ClientSnapshot snapshot) {
        if (snapshot == null) return null;
        return ClientSnapshotResponse.builder()
                .companyName(snapshot.getCompanyName())
                .contactName(snapshot.getContactName())
                .email(snapshot.getEmail())
                .phone(snapshot.getPhone())
                .address(snapshot.getAddress())
                .build();
    }
}
