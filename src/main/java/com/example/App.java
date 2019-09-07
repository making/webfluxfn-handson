package com.example;

import com.example.expenditure.ExpenditureHandler;
import com.example.expenditure.R2dbcExpenditureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
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
        final ConnectionFactory connectionFactory = connectionFactory();
        final DatabaseClient databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build();
        final TransactionalOperator transactionalOperator = TransactionalOperator.create(new R2dbcTransactionManager(connectionFactory));

        initializeDatabase(connectionFactory.getMetadata().getName(), databaseClient).subscribe();

        return new ExpenditureHandler(new R2dbcExpenditureRepository(databaseClient, transactionalOperator)).routes();
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

    static ConnectionFactory connectionFactory() {
        // postgresql://username:password@hostname:5432/dbname
        String databaseUrl = Optional.ofNullable(System.getenv("DATABASE_URL")).orElse("h2:file:///./target/demo?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        return ConnectionFactories.get("r2dbc:" + databaseUrl);
    }

    public static Mono<Void> initializeDatabase(String name, DatabaseClient databaseClient) {
        if ("H2".equals(name)) {
            return databaseClient.execute("CREATE TABLE IF NOT EXISTS expenditure (expenditure_id INT PRIMARY KEY AUTO_INCREMENT, expenditure_name VARCHAR(255), unit_price INT NOT NULL, quantity " +
                "INT NOT NULL, expenditure_date DATE NOT NULL)")
                .then();
        } else if ("PostgreSQL".equals(name)) {
            return databaseClient.execute("CREATE TABLE IF NOT EXISTS expenditure (expenditure_id SERIAL PRIMARY KEY, expenditure_name VARCHAR(255), unit_price INT NOT NULL, quantity INT NOT NULL, " +
                "expenditure_date DATE NOT NULL)")
                .then();
        }
        return Mono.error(new IllegalStateException(name + " is not supported."));
    }
}
