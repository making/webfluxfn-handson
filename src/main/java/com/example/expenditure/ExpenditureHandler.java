package com.example.expenditure;

import com.example.error.ErrorResponseBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ExpenditureHandler {

    private final ExpenditureRepository expenditureRepository;

    public ExpenditureHandler(ExpenditureRepository expenditureRepository) {
        this.expenditureRepository = expenditureRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/expenditures", this::list)
            .POST("/expenditures", this::post)
            .GET("/expenditures/{expenditureId}", this::get)
            .DELETE("/expenditures/{expenditureId}", this::delete)
            .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.expenditureRepository.findAll(), Expenditure.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Expenditure.class)
            .flatMap(expenditure -> expenditure.validate()
                .bimap(v -> new ErrorResponseBuilder().withStatus(BAD_REQUEST).withDetails(v).build(), this.expenditureRepository::save)
                .fold(error -> ServerResponse.badRequest().bodyValue(error),
                    result -> result.flatMap(created -> ServerResponse
                        .created(UriComponentsBuilder.fromUri(req.uri()).path("/{expenditureId}").build(created.getExpenditureId()))
                        .bodyValue(created))));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.expenditureRepository.findById(Integer.valueOf(req.pathVariable("expenditureId")))
            .flatMap(expenditure -> ServerResponse.ok().bodyValue(expenditure))
            .switchIfEmpty(Mono.defer(() -> ServerResponse.status(NOT_FOUND)
                .bodyValue(new ErrorResponseBuilder()
                    .withMessage("The given expenditure is not found.")
                    .withStatus(NOT_FOUND)
                    .build())));
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
            .build(this.expenditureRepository.deleteById(Integer.valueOf(req.pathVariable("expenditureId"))));
    }
}