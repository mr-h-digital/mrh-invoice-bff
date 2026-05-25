package co.za.mrhdigital.invoiceservice.invoice;

import co.za.mrhdigital.invoiceservice.domain.client.Client;
import co.za.mrhdigital.invoiceservice.domain.client.ClientRepository;
import co.za.mrhdigital.invoiceservice.domain.invoice.DiscountType;
import co.za.mrhdigital.invoiceservice.domain.invoice.Invoice;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceRepository;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceService;
import co.za.mrhdigital.invoiceservice.domain.invoice.InvoiceStatus;
import co.za.mrhdigital.invoiceservice.domain.invoice.LineItem;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceRequest;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.InvoiceResponse;
import co.za.mrhdigital.invoiceservice.domain.invoice.dto.LineItemRequest;
import co.za.mrhdigital.invoiceservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private InvoiceRequest buildRequest(boolean vatEnabled, DiscountType discountType, BigDecimal discountValue) {
        return InvoiceRequest.builder()
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .vatEnabled(vatEnabled)
                .vatRate(new BigDecimal("0.15"))
                .discountType(discountType)
                .discountValue(discountValue)
                .lineItems(List.of(
                        LineItemRequest.builder()
                                .name("Service A")
                                .quantity(new BigDecimal("2"))
                                .unitPrice(new BigDecimal("500.00"))
                                .sortOrder(0)
                                .build(),
                        LineItemRequest.builder()
                                .name("Service B")
                                .quantity(new BigDecimal("1"))
                                .unitPrice(new BigDecimal("300.00"))
                                .sortOrder(1)
                                .build()
                ))
                .build();
    }

    @Test
    void createInvoice_calculatesSubtotalCorrectly() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequest request = buildRequest(false, null, BigDecimal.ZERO);
        InvoiceResponse response = invoiceService.create(request);
        assertThat(response.getSubtotal()).isEqualByComparingTo("1300.00");
    }

    @Test
    void createInvoice_appliesPercentDiscount() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequest request = buildRequest(false, DiscountType.PERCENT, new BigDecimal("10"));
        InvoiceResponse response = invoiceService.create(request);
        assertThat(response.getSubtotal()).isEqualByComparingTo("1300.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("130.00");
        assertThat(response.getTotal()).isEqualByComparingTo("1170.00");
    }

    @Test
    void createInvoice_appliesAmountDiscount() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequest request = buildRequest(false, DiscountType.AMOUNT, new BigDecimal("200.00"));
        InvoiceResponse response = invoiceService.create(request);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("200.00");
        assertThat(response.getTotal()).isEqualByComparingTo("1100.00");
    }

    @Test
    void createInvoice_appliesVat() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequest request = buildRequest(true, null, BigDecimal.ZERO);
        InvoiceResponse response = invoiceService.create(request);
        assertThat(response.getVatAmount()).isEqualByComparingTo("195.00");
        assertThat(response.getTotal()).isEqualByComparingTo("1495.00");
    }

    @Test
    void createInvoice_appliesDiscountThenVat() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceRequest request = buildRequest(true, DiscountType.AMOUNT, new BigDecimal("300.00"));
        InvoiceResponse response = invoiceService.create(request);
        // subtotal=1300, discount=300, afterDiscount=1000, vat=150, total=1150
        assertThat(response.getSubtotal()).isEqualByComparingTo("1300.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getVatAmount()).isEqualByComparingTo("150.00");
        assertThat(response.getTotal()).isEqualByComparingTo("1150.00");
    }

    @Test
    void updateStatus_onlyChangesStatus() {
        Invoice invoice = Invoice.builder()
                .id("inv-1")
                .invoiceNumber("INV-2026-001")
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .subtotal(new BigDecimal("1000.00"))
                .total(new BigDecimal("1000.00"))
                .lineItems(new ArrayList<>())
                .build();

        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse response = invoiceService.updateStatus("inv-1", InvoiceStatus.PAID);

        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(response.getTotal()).isEqualByComparingTo("1000.00");
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void deleteInvoice_callsRepository() {
        Invoice invoice = Invoice.builder()
                .id("inv-1")
                .invoiceNumber("INV-2026-001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .lineItems(new ArrayList<>())
                .build();

        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));

        invoiceService.delete("inv-1");

        verify(invoiceRepository).deleteById("inv-1");
    }

    @Test
    void duplicateInvoice_createsNewDraftWithTodaysDate() {
        when(invoiceRepository.findInvoiceNumbersByYear(any())).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDate today = LocalDate.now();

        List<LineItem> sourceItems = new ArrayList<>();
        Invoice source = Invoice.builder()
                .id("source-1")
                .invoiceNumber("INV-2026-001")
                .status(InvoiceStatus.PAID)
                .issueDate(today.minusDays(30))
                .dueDate(today.minusDays(1))
                .subtotal(new BigDecimal("1000.00"))
                .total(new BigDecimal("1000.00"))
                .lineItems(sourceItems)
                .build();

        LineItem li = LineItem.builder()
                .invoice(source)
                .name("Service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("1000.00"))
                .amount(new BigDecimal("1000.00"))
                .sortOrder(0)
                .build();
        sourceItems.add(li);

        when(invoiceRepository.findById("source-1")).thenReturn(Optional.of(source));

        InvoiceResponse response = invoiceService.duplicate("source-1");

        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(response.getIssueDate()).isEqualTo(today);
        assertThat(response.getDueDate()).isEqualTo(today.plusDays(30));
        assertThat(response.getInvoiceNumber()).startsWith("INV-");
        assertThat(response.getLineItems()).hasSize(1);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(invoiceRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> invoiceService.findById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
