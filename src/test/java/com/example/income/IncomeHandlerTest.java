package com.example.income;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest
@Import(IncomeHandler.class)
class IncomeHandlerTest {

    @Configuration
    static class Config {

        @Bean
        public RouterFunction<?> routes(IncomeHandler incomeHandler) {
            return incomeHandler.routes();
        }

        @Bean
        @Primary
        public IncomeRepository incomeRepository() {
            return new InMemoryIncomeRepository();
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient testClient;

    @Autowired
    private InMemoryIncomeRepository incomeRepository;


    private List<Income> fixtures = Arrays.asList(
        new IncomeBuilder()
            .withIncomeId(1)
            .withIncomeName("給与")
            .withAmount(200000)
            .withIncomeDate(LocalDate.of(2019, 4, 15))
            .build(),
        new IncomeBuilder()
            .withIncomeId(2)
            .withIncomeName("ボーナス")
            .withAmount(150000)
            .withIncomeDate(LocalDate.of(2019, 4, 25))
            .build());

    @BeforeAll
    void before() {
        this.testClient = WebTestClient.bindToApplicationContext(this.applicationContext)
            .build();
    }

    @BeforeEach
    void reset() {
        this.incomeRepository.incomes.clear();
        this.incomeRepository.incomes.addAll(this.fixtures);
        this.incomeRepository.counter.set(100);
    }

    @Test
    void list() {
        this.testClient.get()
            .uri("/incomes")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();
                assertThat(body.size()).isEqualTo(2);

                assertThat(body.get(0).get("incomeId").asInt()).isEqualTo(1);
                assertThat(body.get(0).get("incomeName").asText()).isEqualTo("給与");
                assertThat(body.get(0).get("amount").asInt()).isEqualTo(200000);
                assertThat(body.get(0).get("incomeDate").asText()).isEqualTo("2019-04-15");

                assertThat(body.get(1).get("incomeId").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("incomeName").asText()).isEqualTo("ボーナス");
                assertThat(body.get(1).get("amount").asInt()).isEqualTo(150000);
                assertThat(body.get(1).get("incomeDate").asText()).isEqualTo("2019-04-25");
            });
    }

    @Test
    void get_200() {
        this.testClient.get()
            .uri("/incomes/{incomeId}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(1);
                assertThat(body.get("incomeName").asText()).isEqualTo("給与");
                assertThat(body.get("amount").asInt()).isEqualTo(200000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-15");
            });
    }

    @Test
    void get_404() {
        this.testClient.get()
            .uri("/incomes/{incomeId}", 10000)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(404);
                assertThat(body.get("error").asText()).isEqualTo("Not Found");
                assertThat(body.get("message").asText()).isEqualTo("The given income is not found.");
            });
    }

    @Test
    void post_201() {
        Map<String, Object> income = new LinkedHashMap<String, Object>() {

            {
                put("incomeName", "臨時収入");
                put("amount", 250000);
                put("incomeDate", "2019-04-28");
            }
        };
        this.testClient.post()
            .uri("/incomes")
            .bodyValue(income)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                URI location = result.getResponseHeaders().getLocation();
                assertThat(location.toString()).endsWith("/incomes/100");
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(100);
                assertThat(body.get("amount").asInt()).isEqualTo(250000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-28");
            });

        this.testClient.get()
            .uri("/incomes/{incomeId}", 100)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(100);
                assertThat(body.get("incomeName").asText()).isEqualTo("臨時収入");
                assertThat(body.get("amount").asInt()).isEqualTo(250000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-28");
            });
    }

    @Test
    void post_400() {
        Map<String, Object> income = new LinkedHashMap<String, Object>() {

            {
                put("incomeId", 1000);
                put("incomeName", "");
                put("amount", -1);
            }
        };

        this.testClient.post()
            .uri("/incomes")
            .bodyValue(income)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(400);
                assertThat(body.get("error").asText()).isEqualTo("Bad Request");
                assertThat(body.get("details").size()).isEqualTo(4);
                assertThat(body.get("details").get("incomeId").get(0).asText()).isEqualTo("\"incomeId\" must be null");
                assertThat(body.get("details").get("incomeName").get(0).asText()).isEqualTo("\"incomeName\" must not be empty");
                assertThat(body.get("details").get("amount").get(0).asText()).isEqualTo("\"amount\" must be greater than 0");
                assertThat(body.get("details").get("incomeDate").get(0).asText()).isEqualTo("\"incomeDate\" must not be null");
            });
    }

    @Test
    void delete() {
        this.testClient.delete()
            .uri("/incomes/{incomeId}", 1)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);

        this.testClient.get()
            .uri("/incomes/1")
            .exchange()
            .expectStatus().isNotFound();
    }
}