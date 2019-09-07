package com.example.income;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeRepository {

    Flux<Income> findAll();

    Mono<Income> findById(Integer incomeId);

    Mono<Income> save(Income income);

    Mono<Void> deleteById(Integer incomeId);
}