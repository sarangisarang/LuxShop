package com.luxshop.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI luxShopOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("LuxShop API")
                .description("REST API for the LuxShop e-commerce store (catalog, orders, customers, registration).")
                .version("v1")
                .license(new License().name("Demo").url("https://github.com/sarangisarang/LuxShop")));
    }
}
