package com.example.income;

import com.example.App;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class R2dbcIncomeRepositoryTest {

    R2dbcIncomeRepository incomeRepository;

    DatabaseClient databaseClient;

    TransactionalOperator transactionalOperator;

    private List<Income> fixtures = Arrays.asList(
        new IncomeBuilder()
            .withIncomeName("給与")
            .withAmount(200000)
            .withIncomeDate(LocalDate.of(2019, 4, 15))
            .build(),
        new IncomeBuilder()
            .withIncomeName("ボーナス")
            .withAmount(150000)
            .withIncomeDate(LocalDate.of(2019, 4, 25))
            .build());

    @BeforeAll
    void init() {
        final ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        this.databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build();
        this.transactionalOperator = TransactionalOperator.create(new R2dbcTransactionManager(connectionFactory));
        this.incomeRepository = new R2dbcIncomeRepository(this.databaseClient, transactionalOperator);
        App.initializeDatabase("H2", this.databaseClient).block();
    }

    @BeforeEach
    void each() throws Exception {
        this.databaseClient.execute("TRUNCATE TABLE income")
            .then()
            .thenMany(Flux.fromIterable(this.fixtures)
                .flatMap(income -> this.databaseClient.insert()
                    .into(Income.class)
                    .using(income)
                    .then())
                .as(transactionalOperator::transactional))
            .blockLast();
    }

    @Test
    void findAll() {
        StepVerifier.create(this.incomeRepository.findAll())
            .consumeNextWith(income -> {
                assertThat(income.getIncomeId()).isNotNull();
                assertThat(income.getIncomeName()).isEqualTo("給与");
                assertThat(income.getAmount()).isEqualTo(200000);
                assertThat(income.getIncomeDate()).isEqualTo(LocalDate.of(2019, 4, 15));
            })
            .consumeNextWith(income -> {
                assertThat(income.getIncomeId()).isNotNull();
                assertThat(income.getIncomeName()).isEqualTo("ボーナス");
                assertThat(income.getAmount()).isEqualTo(150000);
                assertThat(income.getIncomeDate()).isEqualTo(LocalDate.of(2019, 4, 25));
            })
            .verifyComplete();
    }

    @Test
    void findById() {
        Integer incomeId = this.databaseClient.execute("SELECT income_id FROM income WHERE income_name = :income_name")
            .bind("income_name", "給与")
            .map((row, rowMetadata) -> row.get("income_id", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.incomeRepository.findById(incomeId))
            .consumeNextWith(income -> {
                assertThat(income.getIncomeId()).isNotNull();
                assertThat(income.getIncomeName()).isEqualTo("給与");
                assertThat(income.getAmount()).isEqualTo(200000);
                assertThat(income.getIncomeDate()).isEqualTo(LocalDate.of(2019, 4, 15));
            })
            .verifyComplete();
    }

    @Test
    void findById_Empty() {
        Integer latestId = this.databaseClient.execute("SELECT MAX(income_id) AS max FROM income")
            .map((row, rowMetadata) -> row.get("max", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.incomeRepository.findById(latestId + 1))
            .verifyComplete();
    }

    @Test
    void save() {
        Integer latestId = this.databaseClient.execute("SELECT MAX(income_id) AS max FROM income")
            .map((row, rowMetadata) -> row.get("max", Integer.class))
            .one()
            .block();

        Income create = new IncomeBuilder()
            .withIncomeName("印税")
            .withAmount(80000)
            .withIncomeDate(LocalDate.of(2019, 4, 30))
            .build();

        StepVerifier.create(this.incomeRepository.save(create))
            .consumeNextWith(income -> {
                assertThat(income.getIncomeId()).isGreaterThan(latestId);
                assertThat(income.getIncomeName()).isEqualTo("印税");
                assertThat(income.getAmount()).isEqualTo(80000);
                assertThat(income.getIncomeDate()).isEqualTo(LocalDate.of(2019, 4, 30));
            })
            .verifyComplete();
    }

    @Test
    void deleteById() {
        Integer incomeId = this.databaseClient.execute("SELECT income_id FROM income WHERE income_name = :income_name")
            .bind("income_name", "給与")
            .map((row, rowMetadata) -> row.get("income_id", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.incomeRepository.deleteById(incomeId))
            .verifyComplete();

        StepVerifier.create(this.incomeRepository.findById(incomeId))
            .verifyComplete();
    }
}