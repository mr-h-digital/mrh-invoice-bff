package co.za.mrhdigital.invoiceservice.domain.client.dto;

import co.za.mrhdigital.invoiceservice.domain.client.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private String id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;

    public static ClientResponse from(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .contactName(client.getContactName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
