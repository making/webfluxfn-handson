package com.example.config;

import com.example.expenditure.ExpenditureHandler;
import com.example.income.IncomeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ExpenditureHandler expenditureHandler, IncomeHandler incomeHandler) {
        return expenditureHandler.routes()
            .and(incomeHandler.routes());
    }
}