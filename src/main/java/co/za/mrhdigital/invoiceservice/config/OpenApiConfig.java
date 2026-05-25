package co.za.mrhdigital.invoiceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mr. H Digital — Invoice Service API")
                        .description("BFF API for the Mr. H Digital Invoice Generator. Manages clients, invoices, line items and dashboard statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mr. H Digital")
                                .email("hello@mrhdigital.co.za"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://mrhdigital.co.za")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.mrhdigital.co.za").description("Production")
                ));
    }
}
