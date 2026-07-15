package com.marketplace.professional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProfessionalServicesMarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfessionalServicesMarketplaceApplication.class, args);
    }
}
