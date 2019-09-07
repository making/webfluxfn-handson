package com.example.expenditure;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.r2dbc.query.Criteria.where;

@Repository
public class R2dbcExpenditureRepository implements ExpenditureRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator tx;

    public R2dbcExpenditureRepository(DatabaseClient databaseClient, TransactionalOperator tx) {
        this.databaseClient = databaseClient;
        this.tx = tx;
    }

    @Override
    public Flux<Expenditure> findAll() {
        return this.databaseClient.select().from(Expenditure.class)
            .as(Expenditure.class)
            .all();
    }

    @Override
    public Mono<Expenditure> findById(Integer expenditureId) {
        return this.databaseClient.select().from(Expenditure.class)
            .matching(where("expenditure_id").is(expenditureId))
            .as(Expenditure.class)
            .one();
    }

    @Override
    public Mono<Expenditure> save(Expenditure expenditure) {
        return this.databaseClient.insert().into(Expenditure.class)
            .using(expenditure)
            .fetch()
            .one()
            .map(map -> new ExpenditureBuilder(expenditure)
                .withExpenditureId((Integer) map.get("expenditure_id"))
                .build())
            .as(this.tx::transactional);
    }

    @Override
    public Mono<Void> deleteById(Integer expenditureId) {
        return this.databaseClient.delete().from(Expenditure.class)
            .matching(where("expenditure_id").is(expenditureId))
            .then()
            .as(this.tx::transactional);
    }
}