package co.za.mrhdigital.invoiceservice.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardStats_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalInvoiced", notNullValue()))
                .andExpect(jsonPath("$.data.totalPaid", notNullValue()))
                .andExpect(jsonPath("$.data.totalOutstanding", notNullValue()))
                .andExpect(jsonPath("$.data.totalOverdue", notNullValue()))
                .andExpect(jsonPath("$.data.invoiceCount").isNumber())
                .andExpect(jsonPath("$.data.paidCount").isNumber())
                .andExpect(jsonPath("$.data.outstandingCount").isNumber())
                .andExpect(jsonPath("$.data.overdueCount").isNumber())
                .andExpect(jsonPath("$.data.recentInvoices").isArray());
    }

    @Test
    void getDashboardStats_seededDataPresent() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceCount", greaterThanOrEqualTo(1)));
    }
}
