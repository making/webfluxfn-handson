package com.example.income;

import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.function.Function;

public class R2dbcIncomeRepository implements IncomeRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator tx;

    @SuppressWarnings("ConstantConditions")
    private final Function<Row, Income> rowIncomeMapper = row -> new IncomeBuilder()
        .withIncomeId(row.get("income_id", Integer.class))
        .withIncomeName(row.get("income_name", String.class))
        .withAmount(row.get("amount", Integer.class))
        .withIncomeDate(row.get("income_date", LocalDate.class))
        .build();

    public R2dbcIncomeRepository(DatabaseClient databaseClient, TransactionalOperator tx) {
        this.databaseClient = databaseClient;
        this.tx = tx;
    }

    @Override
    public Flux<Income> findAll() {
        return this.databaseClient.execute("SELECT income_id, income_name, amount, income_date FROM income")
            .map(this.rowIncomeMapper)
            .all();
    }

    @Override
    public Mono<Income> findById(Integer incomeId) {
        return this.databaseClient
            .execute("SELECT income_id, income_name, amount, income_date FROM income WHERE income_id = :income_id")
            .bind("income_id", incomeId)
            .map(this.rowIncomeMapper)
            .one();
    }

    @Override
    public Mono<Income> save(Income income) {
        final String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.contains("postgres") /* TODO 🤔 */) {
            return this.databaseClient
                .execute("INSERT INTO income(income_name, amount, income_date) VALUES(:income_name, :amount, :income_date) RETURNING " +
                    "income_id")
                .bind("income_name", income.getIncomeName())
                .bind("amount", income.getAmount())
                .bind("income_date", income.getIncomeDate())
                .fetch()
                .one()
                .map(map -> new IncomeBuilder(income)
                    .withIncomeId((Integer) map.get("income_id"))
                    .build())
                .as(this.tx::transactional);
        } else {
            return this.databaseClient
                .execute("INSERT INTO income(income_name, amount, income_date) VALUES(:income_name, :amount, :income_date)")
                .bind("income_name", income.getIncomeName())
                .bind("amount", income.getAmount())
                .bind("income_date", income.getIncomeDate())
                .then()
                .then(/* TODO 🤔 */ this.databaseClient.execute("CALL SCOPE_IDENTITY()").fetch().one()
                    .map(map -> new IncomeBuilder(income)
                        .withIncomeId(((Long) map.get("SCOPE_IDENTITY()")).intValue())
                        .build()))
                .as(this.tx::transactional);
        }
    }

    @Override
    public Mono<Void> deleteById(Integer incomeId) {
        return this.databaseClient.execute("DELETE FROM income WHERE income_id = :income_id")
            .bind("income_id", incomeId)
            .then()
            .as(this.tx::transactional);
    }
}