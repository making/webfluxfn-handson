package com.example.expenditure;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ExpenditureHandler {

    private final ExpenditureRepository expenditureRepository;

    public ExpenditureHandler(ExpenditureRepository expenditureRepository) {
        this.expenditureRepository = expenditureRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            // TODO Routingの定義
            // GET /expenditures
            // POST /expenditures
            // GET /expenditures/{expenditureId}
            .DELETE("/expenditures/{expenditureId}", this::delete)
            .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.expenditureRepository.findAll(), Expenditure.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Expenditure.class)
            // TODO
            // ExpenditureRepositoryでExpenditureを保存
            // Hint: ExpenditureRepository.saveを使ってください。
            .flatMap(created -> ServerResponse
                .created(UriComponentsBuilder.fromUri(req.uri()).path("/{expenditureId}").build(created.getExpenditureId()))
                .bodyValue(created));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.expenditureRepository.findById(Integer.valueOf(req.pathVariable("expenditureId")))
            .flatMap(expenditure -> ServerResponse.ok().bodyValue(expenditure))
            // TODO
            // expenditureが存在しない場合は404を返す。エラーレスポンスは{"status":404,"error":"Not Found","message":"The given expenditure is not found."}
            // Hint: switchIfEmptyおよびServerResponse.statusを使ってください。
            ;
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
            .build(this.expenditureRepository.deleteById(Integer.valueOf(req.pathVariable("expenditureId"))));
    }
}