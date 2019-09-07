package com.example.income;

import com.example.error.ErrorResponseBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class IncomeHandler {

    private final IncomeRepository incomeRepository;

    public IncomeHandler(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/incomes", this::list)
            .POST("/incomes", this::post)
            .GET("/incomes/{incomeId}", this::get)
            .DELETE("/incomes/{incomeId}", this::delete)
            .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.incomeRepository.findAll(), Income.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Income.class)
            .flatMap(income -> income.validate()
                .bimap(v -> new ErrorResponseBuilder().withStatus(BAD_REQUEST).withDetails(v).build(), this.incomeRepository::save)
                .fold(error -> ServerResponse.badRequest().bodyValue(error),
                    result -> result.flatMap(created -> ServerResponse
                        .created(UriComponentsBuilder.fromUri(req.uri()).path("/{incomeId}").build(created.getIncomeId()))
                        .bodyValue(created))));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.incomeRepository.findById(Integer.valueOf(req.pathVariable("incomeId")))
            .flatMap(income -> ServerResponse.ok().bodyValue(income))
            .switchIfEmpty(Mono.defer(() -> ServerResponse.status(NOT_FOUND)
                .bodyValue(new ErrorResponseBuilder()
                    .withMessage("The given income is not found.")
                    .withStatus(NOT_FOUND)
                    .build())));
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
            .build(this.incomeRepository.deleteById(Integer.valueOf(req.pathVariable("incomeId"))));
    }
}