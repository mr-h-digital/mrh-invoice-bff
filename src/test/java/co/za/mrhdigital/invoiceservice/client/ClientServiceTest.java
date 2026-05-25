package co.za.mrhdigital.invoiceservice.client;

import co.za.mrhdigital.invoiceservice.domain.client.Client;
import co.za.mrhdigital.invoiceservice.domain.client.ClientRepository;
import co.za.mrhdigital.invoiceservice.domain.client.ClientService;
import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientRequest;
import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceRepository;
import co.za.mrhdigital.invoiceservice.exception.ConflictException;
import co.za.mrhdigital.invoiceservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_savesAndReturnsResponse() {
        ClientRequest request = ClientRequest.builder()
                .companyName("ACME Ltd")
                .contactName("Alice")
                .email("alice@acme.co.za")
                .phone("+27 11 000 0000")
                .build();

        Client saved = Client.builder()
                .id("new-id")
                .companyName("ACME Ltd")
                .contactName("Alice")
                .email("alice@acme.co.za")
                .phone("+27 11 000 0000")
                .build();

        when(clientRepository.save(any())).thenReturn(saved);

        ClientResponse response = clientService.create(request);

        assertThat(response.getId()).isEqualTo("new-id");
        assertThat(response.getCompanyName()).isEqualTo("ACME Ltd");
        verify(clientRepository).save(any());
    }

    @Test
    void findById_returnsClient() {
        Client client = Client.builder()
                .id("c-1")
                .companyName("ACME Ltd")
                .contactName("Alice")
                .email("alice@acme.co.za")
                .build();
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));

        ClientResponse response = clientService.findById("c-1");
        assertThat(response.getId()).isEqualTo("c-1");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(clientRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> clientService.findById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteClient_withInvoices_throwsConflict() {
        Client client = Client.builder().id("c-1").companyName("x").contactName("x").email("x@x.com").build();
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(invoiceRepository.existsByClientId("c-1")).thenReturn(true);

        assertThatThrownBy(() -> clientService.delete("c-1"))
                .isInstanceOf(ConflictException.class);

        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void deleteClient_withoutInvoices_deletesSuccessfully() {
        Client client = Client.builder().id("c-1").companyName("x").contactName("x").email("x@x.com").build();
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(invoiceRepository.existsByClientId("c-1")).thenReturn(false);

        clientService.delete("c-1");

        verify(clientRepository).deleteById("c-1");
    }

    @Test
    void findAll_withSearch_callsSearchMethod() {
        when(clientRepository.search("ACME")).thenReturn(List.of());
        List<ClientResponse> result = clientService.findAll("ACME");
        assertThat(result).isEmpty();
        verify(clientRepository).search("ACME");
    }

    @Test
    void findAll_withoutSearch_callsFindAll() {
        when(clientRepository.findAll()).thenReturn(List.of());
        List<ClientResponse> result = clientService.findAll(null);
        assertThat(result).isEmpty();
        verify(clientRepository).findAll();
    }
}
