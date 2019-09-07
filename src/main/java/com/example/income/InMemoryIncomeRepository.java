package com.example.income;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryIncomeRepository implements IncomeRepository {

    final List<Income> incomes = new CopyOnWriteArrayList<>();

    final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public Flux<Income> findAll() {
        return Flux.fromIterable(this.incomes);
    }

    @Override
    public Mono<Income> findById(Integer incomeId) {
        return Mono.justOrEmpty(this.incomes.stream()
            .filter(x -> Objects.equals(x.getIncomeId(), incomeId))
            .findFirst());
    }

    @Override
    public Mono<Income> save(Income income) {
        return Mono.fromCallable(() -> {
            Income created = new IncomeBuilder(income)
                .withIncomeId(this.counter.getAndIncrement())
                .build();
            this.incomes.add(created);
            return created;
        });
    }

    @Override
    public Mono<Void> deleteById(Integer incomeId) {
        return Mono.defer(() -> {
            this.incomes.removeIf(x -> Objects.equals(x.getIncomeId(), incomeId));
            return Mono.empty();
        });
    }
}