package com.example.income;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.r2dbc.query.Criteria.where;

@Repository
public class R2dbcIncomeRepository implements IncomeRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator tx;

    public R2dbcIncomeRepository(DatabaseClient databaseClient, TransactionalOperator tx) {
        this.databaseClient = databaseClient;
        this.tx = tx;
    }

    @Override
    public Flux<Income> findAll() {
        return this.databaseClient.select().from(Income.class)
            .as(Income.class)
            .all();
    }

    @Override
    public Mono<Income> findById(Integer incomeId) {
        return this.databaseClient.select().from(Income.class)
            .matching(where("income_id").is(incomeId))
            .as(Income.class)
            .one();
    }

    @Override
    public Mono<Income> save(Income income) {
        return this.databaseClient.insert().into(Income.class)
            .using(income)
            .fetch()
            .one()
            .map(map -> new IncomeBuilder(income)
                .withIncomeId((Integer) map.get("income_id"))
                .build())
            .as(this.tx::transactional);
    }

    @Override
    public Mono<Void> deleteById(Integer incomeId) {
        return this.databaseClient.delete().from(Income.class)
            .matching(where("income_id").is(incomeId))
            .then()
            .as(this.tx::transactional);
    }
}