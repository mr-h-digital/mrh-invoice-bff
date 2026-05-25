package co.za.mrhdigital.invoiceservice.domain.invoice;

import co.za.mrhdigital.invoiceservice.common.ApiResponse;
import co.za.mrhdigital.invoiceservice.common.PageResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceRequest;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/next-number")
    @Operation(summary = "Get next available invoice number")
    public ResponseEntity<ApiResponse<Map<String, String>>> nextNumber() {
        String number = invoiceService.generateNextInvoiceNumber();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("invoiceNumber", number)));
    }

    @GetMapping
    @Operation(summary = "List invoices with optional filtering and pagination")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> list(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<InvoiceResponse> result = invoiceService.findAll(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @PostMapping
    @Operation(summary = "Create an invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(invoiceService.create(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete invoice")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate invoice as new DRAFT with today's date")
    public ResponseEntity<ApiResponse<InvoiceResponse>> duplicate(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(invoiceService.duplicate(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update invoice status only")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.updateStatus(id, request.getStatus())));
    }
}
