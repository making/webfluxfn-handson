package com.example.expenditure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenditureRepository {

    Flux<Expenditure> findAll();

    Mono<Expenditure> findById(Integer expenditureId);

    Mono<Expenditure> save(Expenditure expenditure);

    Mono<Void> deleteById(Integer expenditureId);
}