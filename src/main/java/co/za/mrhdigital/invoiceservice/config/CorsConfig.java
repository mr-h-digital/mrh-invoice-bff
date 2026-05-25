package co.za.mrhdigital.invoiceservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var registration = registry.addMapping("/api/**")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);

                if (StringUtils.hasText(frontendUrl)) {
                    registration.allowedOrigins(
                            "http://localhost:5173",
                            "http://localhost:3000",
                            frontendUrl
                    );
                } else {
                    registration.allowedOrigins(
                            "http://localhost:5173",
                            "http://localhost:3000"
                    );
                }
            }
        };
    }
}
