package co.za.mrhdigital.invoiceservice.client;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String createdClientId;

    @Test
    @Order(1)
    void createClient_returns201() throws Exception {
        String payload = """
                {
                  "companyName": "Test Corp",
                  "contactName": "John Doe",
                  "email": "john@testcorp.co.za",
                  "phone": "+27 11 000 0000",
                  "address": "1 Test Street, Johannesburg"
                }
                """;

        String response = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.companyName", is("Test Corp")))
                .andReturn().getResponse().getContentAsString();

        createdClientId = objectMapper.readTree(response).path("data").path("id").asText();
    }

    @Test
    @Order(2)
    void listClients_returnsClients() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(3)
    void getClientById_returnsClient() throws Exception {
        mockMvc.perform(get("/api/clients/" + createdClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(createdClientId)))
                .andExpect(jsonPath("$.data.companyName", is("Test Corp")));
    }

    @Test
    @Order(4)
    void updateClient_updatesFields() throws Exception {
        String payload = """
                {
                  "companyName": "Test Corp Updated",
                  "contactName": "Jane Doe",
                  "email": "jane@testcorp.co.za"
                }
                """;

        mockMvc.perform(put("/api/clients/" + createdClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName", is("Test Corp Updated")))
                .andExpect(jsonPath("$.data.contactName", is("Jane Doe")));
    }

    @Test
    @Order(5)
    void searchClients_returnsMatchingClients() throws Exception {
        mockMvc.perform(get("/api/clients?search=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @Order(6)
    void deleteClient_withoutInvoices_returns204() throws Exception {
        mockMvc.perform(delete("/api/clients/" + createdClientId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void deleteClient_thatDoesNotExist_returns404() throws Exception {
        mockMvc.perform(delete("/api/clients/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void deleteClient_withInvoices_returns409() throws Exception {
        // The seeded client 'client-tveco-001' has invoices from V4 migration
        mockMvc.perform(delete("/api/clients/client-tveco-001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @Order(9)
    void getClientInvoices_returnsInvoiceList() throws Exception {
        mockMvc.perform(get("/api/clients/client-tveco-001/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(10)
    void createClient_withInvalidEmail_returns400() throws Exception {
        String payload = """
                {
                  "companyName": "Bad Corp",
                  "contactName": "Bad Guy",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
