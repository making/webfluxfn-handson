package com.example.income;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;

@JsonPOJOBuilder
public class IncomeBuilder {

    private int amount;

    private LocalDate incomeDate;

    private Integer incomeId;

    private String incomeName;

    public IncomeBuilder() {
    }

    public IncomeBuilder(Income income) {
        this.amount = income.getAmount();
        this.incomeDate = income.getIncomeDate();
        this.incomeId = income.getIncomeId();
        this.incomeName = income.getIncomeName();
    }

    public Income build() {
        return new Income(incomeId, incomeName, amount, incomeDate);
    }

    public IncomeBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public IncomeBuilder withIncomeDate(LocalDate incomeDate) {
        this.incomeDate = incomeDate;
        return this;
    }

    public IncomeBuilder withIncomeId(Integer incomeId) {
        this.incomeId = incomeId;
        return this;
    }

    public IncomeBuilder withIncomeName(String incomeName) {
        this.incomeName = incomeName;
        return this;
    }
}