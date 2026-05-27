package co.za.mrhdigital.invoiceservice.domain.invoice;

import co.za.mrhdigital.invoiceservice.domain.client.Client;
import co.za.mrhdigital.invoiceservice.domain.client.ClientRepository;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceRequest;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.LineItemRequest;
import co.za.mrhdigital.invoiceservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;

    public Page<InvoiceResponse> findAll(InvoiceStatus status, String search, Pageable pageable) {
        return invoiceRepository.findAllFiltered(status, search, pageable)
                .map(InvoiceResponse::from);
    }

    public InvoiceResponse findById(String id) {
        return InvoiceResponse.from(getOrThrow(id));
    }

    public List<InvoiceResponse> findByClientId(String clientId) {
        return invoiceRepository.findByClientId(clientId)
                .stream().map(InvoiceResponse::from).toList();
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        String invoiceNumber = StringUtils.hasText(request.getInvoiceNumber())
                ? request.getInvoiceNumber()
                : generateNextInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.DRAFT)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .discountType(request.getDiscountType())
                .discountValue(nullSafe(request.getDiscountValue()))
                .vatEnabled(request.isVatEnabled())
                .vatRate(nullSafe(request.getVatRate(), new BigDecimal("0.15")))
                .notes(request.getNotes())
                .build();

        applyClient(invoice, request.getClientId());
        applyPaymentDetails(invoice, request);
        applyLineItems(invoice, request.getLineItems());
        recalculate(invoice);

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse update(String id, InvoiceRequest request) {
        Invoice invoice = getOrThrow(id);

        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setDiscountType(request.getDiscountType());
        invoice.setDiscountValue(nullSafe(request.getDiscountValue()));
        invoice.setVatEnabled(request.isVatEnabled());
        invoice.setVatRate(nullSafe(request.getVatRate(), new BigDecimal("0.15")));
        invoice.setNotes(request.getNotes());

        applyClient(invoice, request.getClientId());
        applyPaymentDetails(invoice, request);

        invoice.getLineItems().clear();
        applyLineItems(invoice, request.getLineItems());
        recalculate(invoice);

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse updateStatus(String id, InvoiceStatus status) {
        Invoice invoice = getOrThrow(id);
        invoice.setStatus(status);
        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public void delete(String id) {
        getOrThrow(id);
        invoiceRepository.deleteById(id);
    }

    @Transactional
    public InvoiceResponse duplicate(String id) {
        Invoice source = getOrThrow(id);
        LocalDate today = LocalDate.now();

        Invoice copy = Invoice.builder()
                .invoiceNumber(generateNextInvoiceNumber())
                .status(InvoiceStatus.DRAFT)
                .issueDate(today)
                .dueDate(today.plusDays(30))
                .client(source.getClient())
                .clientSnapshot(source.getClientSnapshot())
                .discountType(source.getDiscountType())
                .discountValue(source.getDiscountValue())
                .vatEnabled(source.isVatEnabled())
                .vatRate(source.getVatRate())
                .notes(source.getNotes())
                .paymentDetails(source.getPaymentDetails())
                .build();

        for (LineItem sourceItem : source.getLineItems()) {
            LineItem copyItem = LineItem.builder()
                    .invoice(copy)
                    .name(sourceItem.getName())
                    .description(sourceItem.getDescription())
                    .quantity(sourceItem.getQuantity())
                    .unitPrice(sourceItem.getUnitPrice())
                    .amount(sourceItem.getAmount())
                    .sortOrder(sourceItem.getSortOrder())
                    .build();
            copy.getLineItems().add(copyItem);
        }

        recalculate(copy);
        return InvoiceResponse.from(invoiceRepository.save(copy));
    }

    public String generateNextInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        List<String> existing = invoiceRepository.findInvoiceNumbersByYear(year);

        int next = 1;
        if (!existing.isEmpty()) {
            String last = existing.get(0);
            try {
                String[] parts = last.split("-");
                next = Integer.parseInt(parts[parts.length - 1]) + 1;
            } catch (NumberFormatException ignored) {
                next = existing.size() + 1;
            }
        }

        return String.format("INV-%s-%03d", year, next);
    }

    private void applyClient(Invoice invoice, String clientId) {
        if (StringUtils.hasText(clientId)) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
            invoice.setClient(client);
            invoice.setClientSnapshot(ClientSnapshot.builder()
                    .companyName(client.getCompanyName())
                    .contactName(client.getContactName())
                    .email(client.getEmail())
                    .phone(client.getPhone())
                    .address(client.getAddress())
                    .build());
        }
    }

    private void applyPaymentDetails(Invoice invoice, InvoiceRequest request) {
        if (request.getPaymentDetails() != null) {
            var pd = request.getPaymentDetails();
            invoice.setPaymentDetails(PaymentDetails.builder()
                    .bank(pd.getBank())
                    .accountName(pd.getAccountName())
                    .accountNumber(pd.getAccountNumber())
                    .accountType(pd.getAccountType())
                    .branchCode(pd.getBranchCode())
                    .reference(pd.getReference())
                    .build());
        }
    }

    private void applyLineItems(Invoice invoice, List<LineItemRequest> requests) {
        if (requests == null) return;
        for (int i = 0; i < requests.size(); i++) {
            LineItemRequest req = requests.get(i);
            LineItem item = LineItem.builder()
                    .invoice(invoice)
                    .name(req.getName())
                    .description(req.getDescription())
                    .quantity(nullSafe(req.getQuantity(), BigDecimal.ONE))
                    .unitPrice(nullSafe(req.getUnitPrice()))
                    .sortOrder(req.getSortOrder() != 0 ? req.getSortOrder() : i)
                    .build();
            item.setAmount(item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            invoice.getLineItems().add(item);
        }
    }

    private void recalculate(Invoice invoice) {
        BigDecimal subtotal = invoice.getLineItems().stream()
                .map(li -> li.getQuantity().multiply(li.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (invoice.getDiscountType() != null) {
            BigDecimal dv = nullSafe(invoice.getDiscountValue());
            if (invoice.getDiscountType() == DiscountType.AMOUNT) {
                discountAmount = dv;
            } else if (invoice.getDiscountType() == DiscountType.PERCENT) {
                discountAmount = subtotal.multiply(dv).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }
        invoice.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = BigDecimal.ZERO;
        if (invoice.isVatEnabled()) {
            BigDecimal rate = nullSafe(invoice.getVatRate(), new BigDecimal("0.15"));
            vatAmount = afterDiscount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        invoice.setVatAmount(vatAmount);
        invoice.setTotal(afterDiscount.add(vatAmount).setScale(2, RoundingMode.HALF_UP));

        for (LineItem li : invoice.getLineItems()) {
            li.setAmount(li.getQuantity().multiply(li.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal nullSafe(BigDecimal value, BigDecimal defaultValue) {
        return value != null ? value : defaultValue;
    }

    private Invoice getOrThrow(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }
}
