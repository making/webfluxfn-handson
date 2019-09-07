package com.example.income;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Either;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

@JsonDeserialize(builder = IncomeBuilder.class)
public class Income {

    private final Integer incomeId;

    private final String incomeName;

    private final int amount;

    private final LocalDate incomeDate;

    private static final Validator<Income> validator = ValidatorBuilder.of(Income.class)
        .constraint(Income::getIncomeId, "incomeId", c -> c.isNull())
        .constraint(Income::getIncomeName, "incomeName", c -> c.notEmpty().lessThanOrEqual(255))
        .constraint(Income::getAmount, "amount", c -> c.greaterThan(0))
        .constraintOnObject(Income::getIncomeDate, "incomeDate", c -> c.notNull())
        .build();

    public Income(Integer incomeId, String incomeName, int amount, LocalDate incomeDate) {
        this.incomeId = incomeId;
        this.incomeName = incomeName;
        this.amount = amount;
        this.incomeDate = incomeDate;
    }

    public Integer getIncomeId() {
        return incomeId;
    }

    public String getIncomeName() {
        return incomeName;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDate getIncomeDate() {
        return incomeDate;
    }

    public Either<ConstraintViolations, Income> validate() {
        return validator.validateToEither(this);
    }

    @Override
    public String toString() {
        return "Income{" +
            "incomeId=" + incomeId +
            ", incomeName='" + incomeName + '\'' +
            ", amount=" + amount +
            ", incomeDate=" + incomeDate +
            '}';
    }
}