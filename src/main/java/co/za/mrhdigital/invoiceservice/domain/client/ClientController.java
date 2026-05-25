package co.za.mrhdigital.invoiceservice.domain.client;

import co.za.mrhdigital.invoiceservice.common.ApiResponse;
import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientRequest;
import co.za.mrhdigital.invoiceservice.domain.client.dto.ClientResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceRepository;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints")
public class ClientController {

    private final ClientService clientService;
    private final InvoiceRepository invoiceRepository;

    @GetMapping
    @Operation(summary = "List all clients", description = "Returns all clients, optionally filtered by search query")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> list(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.findAll(search)));
    }

    @PostMapping
    @Operation(summary = "Create a client")
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(clientService.create(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client", description = "Fails with 409 if the client has invoices")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/invoices")
    @Operation(summary = "List invoices for a client")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(@PathVariable String id) {
        clientService.findById(id);
        List<InvoiceResponse> invoices = invoiceRepository.findByClientId(id)
                .stream().map(InvoiceResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(invoices));
    }
}
