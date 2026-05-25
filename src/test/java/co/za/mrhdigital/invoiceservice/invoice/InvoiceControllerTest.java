package co.za.mrhdigital.invoiceservice.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String createdInvoiceId;

    private String buildInvoicePayload() {
        return """
                {
                  "issueDate": "2026-05-22",
                  "dueDate": "2026-06-22",
                  "vatEnabled": false,
                  "vatRate": 0.15,
                  "discountType": "AMOUNT",
                  "discountValue": 100.00,
                  "notes": "Test invoice",
                  "lineItems": [
                    {
                      "name": "Web Design",
                      "description": "Full website design",
                      "quantity": 1,
                      "unitPrice": 3000.00,
                      "sortOrder": 0
                    },
                    {
                      "name": "Hosting",
                      "description": "Annual hosting",
                      "quantity": 1,
                      "unitPrice": 500.00,
                      "sortOrder": 1
                    }
                  ]
                }
                """;
    }

    @Test
    @Order(1)
    void getNextNumber_returnsFormattedNumber() throws Exception {
        mockMvc.perform(get("/api/invoices/next-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.invoiceNumber", matchesPattern("INV-\\d{4}-\\d{3}")));
    }

    @Test
    @Order(2)
    void createInvoice_returns201WithCalculatedTotals() throws Exception {
        String response = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildInvoicePayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.subtotal", is(3500.00)))
                .andExpect(jsonPath("$.data.discountAmount", is(100.00)))
                .andExpect(jsonPath("$.data.total", is(3400.00)))
                .andExpect(jsonPath("$.data.lineItems", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        createdInvoiceId = objectMapper.readTree(response).path("data").path("id").asText();
    }

    @Test
    @Order(3)
    void listInvoices_returnsPaginatedResult() throws Exception {
        mockMvc.perform(get("/api/invoices?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", notNullValue()))
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @Order(4)
    void getInvoiceById_returnsInvoice() throws Exception {
        mockMvc.perform(get("/api/invoices/" + createdInvoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(createdInvoiceId)))
                .andExpect(jsonPath("$.data.status", is("DRAFT")));
    }

    @Test
    @Order(5)
    void updateInvoice_recalculatesTotals() throws Exception {
        String updatedPayload = """
                {
                  "issueDate": "2026-05-22",
                  "dueDate": "2026-06-30",
                  "vatEnabled": true,
                  "vatRate": 0.15,
                  "lineItems": [
                    {
                      "name": "Updated Service",
                      "quantity": 2,
                      "unitPrice": 1000.00,
                      "sortOrder": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/invoices/" + createdInvoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotal", is(2000.00)))
                .andExpect(jsonPath("$.data.vatAmount", is(300.00)))
                .andExpect(jsonPath("$.data.total", is(2300.00)));
    }

    @Test
    @Order(6)
    void updateStatus_changesStatusOnly() throws Exception {
        mockMvc.perform(patch("/api/invoices/" + createdInvoiceId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SENT")));
    }

    @Test
    @Order(7)
    void duplicateInvoice_createsNewDraft() throws Exception {
        mockMvc.perform(post("/api/invoices/" + createdInvoiceId + "/duplicate"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("DRAFT")))
                .andExpect(jsonPath("$.data.id", not(createdInvoiceId)));
    }

    @Test
    @Order(8)
    void deleteInvoice_returns204() throws Exception {
        mockMvc.perform(delete("/api/invoices/" + createdInvoiceId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void getDeletedInvoice_returns404() throws Exception {
        mockMvc.perform(get("/api/invoices/" + createdInvoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @Order(10)
    void filterByStatus_returnsCorrectInvoices() throws Exception {
        mockMvc.perform(get("/api/invoices?status=DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
