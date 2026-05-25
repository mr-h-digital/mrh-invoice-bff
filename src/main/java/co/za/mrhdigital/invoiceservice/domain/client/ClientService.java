package co.za.mrhdigital.invoiceservice.domain.client;

import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientRequest;
import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceRepository;
import co.za.mrhdigital.invoiceservice.exception.ConflictException;
import co.za.mrhdigital.invoiceservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public List<ClientResponse> findAll(String search) {
        List<Client> clients = StringUtils.hasText(search)
                ? clientRepository.search(search)
                : clientRepository.findAll();
        return clients.stream().map(ClientResponse::from).toList();
    }

    public ClientResponse findById(String id) {
        return ClientResponse.from(getOrThrow(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(String id, ClientRequest request) {
        Client client = getOrThrow(id);
        client.setCompanyName(request.getCompanyName());
        client.setContactName(request.getContactName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(String id) {
        getOrThrow(id);
        if (invoiceRepository.existsByClientId(id)) {
            throw new ConflictException("Cannot delete client with existing invoices");
        }
        clientRepository.deleteById(id);
    }

    private Client getOrThrow(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }
}
