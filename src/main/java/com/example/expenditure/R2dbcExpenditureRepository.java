package com.example.expenditure;

import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.function.Function;

public class R2dbcExpenditureRepository implements ExpenditureRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator tx;

    @SuppressWarnings("ConstantConditions")
    private final Function<Row, Expenditure> rowExpenditureMapper = row -> new ExpenditureBuilder()
        .withExpenditureId(row.get("expenditure_id", Integer.class))
        .withExpenditureName(row.get("expenditure_name", String.class))
        .withQuantity(row.get("quantity", Integer.class))
        .withUnitPrice(row.get("unit_price", Integer.class))
        .withExpenditureDate(row.get("expenditure_date", LocalDate.class))
        .build();

    public R2dbcExpenditureRepository(DatabaseClient databaseClient, TransactionalOperator tx) {
        this.databaseClient = databaseClient;
        this.tx = tx;
    }

    @Override
    public Flux<Expenditure> findAll() {
        return this.databaseClient.execute("SELECT expenditure_id, expenditure_name, unit_price, quantity, expenditure_date FROM expenditure")
            .map(this.rowExpenditureMapper)
            .all();
    }

    @Override
    public Mono<Expenditure> findById(Integer expenditureId) {
        return this.databaseClient
            .execute("SELECT expenditure_id, expenditure_name, unit_price, quantity, expenditure_date FROM expenditure WHERE expenditure_id = :expenditure_id")
            .bind("expenditure_id", expenditureId)
            .map(this.rowExpenditureMapper)
            .one();
    }

    @Override
    public Mono<Expenditure> save(Expenditure expenditure) {
        final String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.contains("postgres") /* TODO 🤔 */) {
            return this.databaseClient
                .execute("INSERT INTO expenditure(expenditure_name, unit_price, quantity, expenditure_date) VALUES(:expenditure_name, :unit_price, :quantity, :expenditure_date) RETURNING " +
                    "expenditure_id")
                .bind("expenditure_name", expenditure.getExpenditureName())
                .bind("unit_price", expenditure.getUnitPrice())
                .bind("quantity", expenditure.getQuantity())
                .bind("expenditure_date", expenditure.getExpenditureDate())
                .fetch()
                .one()
                .map(map -> new ExpenditureBuilder(expenditure)
                    .withExpenditureId((Integer) map.get("expenditure_id"))
                    .build())
                .as(this.tx::transactional);
        } else {
            return this.databaseClient
                .execute("INSERT INTO expenditure(expenditure_name, unit_price, quantity, expenditure_date) VALUES(:expenditure_name, :unit_price, :quantity, :expenditure_date)")
                .bind("expenditure_name", expenditure.getExpenditureName())
                .bind("unit_price", expenditure.getUnitPrice())
                .bind("quantity", expenditure.getQuantity())
                .bind("expenditure_date", expenditure.getExpenditureDate())
                .then()
                .then(/* TODO 🤔 */ this.databaseClient.execute("CALL SCOPE_IDENTITY()").fetch().one()
                    .map(map -> new ExpenditureBuilder(expenditure)
                        .withExpenditureId(((Long) map.get("SCOPE_IDENTITY()")).intValue())
                        .build()))
                .as(this.tx::transactional);
        }
    }

    @Override
    public Mono<Void> deleteById(Integer expenditureId) {
        return this.databaseClient.execute("DELETE FROM expenditure WHERE expenditure_id = :expenditure_id")
            .bind("expenditure_id", expenditureId)
            .then()
            .as(this.tx::transactional);
    }
}