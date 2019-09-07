package com.example.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ErrorResponseExceptionHandler implements WebExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(ErrorResponseExceptionHandler.class);

    private ServerResponse.Context context = new ServerResponse.Context() {

        private List<HttpMessageWriter<?>> messageWriters = Collections.singletonList(new EncoderHttpMessageWriter<>(new Jackson2JsonEncoder()));

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return Collections.emptyList();
        }
    };

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.warn(ex.getMessage(), ex);
        final HttpStatus status = (ex instanceof ResponseStatusException) ? ((ResponseStatusException) ex).getStatus() : INTERNAL_SERVER_ERROR;
        return ServerResponse.status(status)
            .contentType(APPLICATION_JSON)
            .bodyValue(new ErrorResponseBuilder()
                .withStatus(status)
                .withMessage(ex.getMessage())
                .build())
            .flatMap(response -> response.writeTo(exchange, this.context));
    }
}