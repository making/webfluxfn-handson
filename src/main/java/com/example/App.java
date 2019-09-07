package com.example;

import com.example.expenditure.ExpenditureHandler;
import com.example.expenditure.InMemoryExpenditureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.Optional;

public class App {

    public static void main(String[] args) throws Exception {
        long begin = System.currentTimeMillis();
        int port = Optional.ofNullable(System.getenv("PORT")) //
            .map(Integer::parseInt) //
            .orElse(8080);

        HttpHandler httpHandler = RouterFunctions.toHttpHandler(App.routes(),
            App.handlerStrategies());
        HttpServer httpServer = HttpServer.create().host("0.0.0.0").port(port)
            .handle(new ReactorHttpHandlerAdapter(httpHandler));
        httpServer.bindUntilJavaShutdown(Duration.ofSeconds(3), disposableServer -> {
            long elapsed = System.currentTimeMillis() - begin;
            LoggerFactory.getLogger(App.class).info("Started in {} seconds",
                elapsed / 1000.0);
        });
    }

    static RouterFunction<ServerResponse> routes() {
        return new ExpenditureHandler(new InMemoryExpenditureRepository()).routes();
    }

    public static HandlerStrategies handlerStrategies() {
        return HandlerStrategies.empty()
            .codecs(configure -> {
                configure.registerDefaults(true);
                ServerCodecConfigurer.ServerDefaultCodecs defaults = configure
                    .defaultCodecs();
                ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                    .dateFormat(new StdDateFormat())
                    .build();
                defaults.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                defaults.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
            })
            .build();
    }
}
