package com.example.expenditure;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;

@JsonPOJOBuilder
public class ExpenditureBuilder {

    private int unitPrice;

    private LocalDate expenditureDate;

    private Integer expenditureId;

    private String expenditureName;

    private int quantity;

    public ExpenditureBuilder() {
    }

    public ExpenditureBuilder(Expenditure expenditure) {
        this.unitPrice = expenditure.getUnitPrice();
        this.expenditureDate = expenditure.getExpenditureDate();
        this.expenditureId = expenditure.getExpenditureId();
        this.expenditureName = expenditure.getExpenditureName();
        this.quantity = expenditure.getQuantity();
    }

    public Expenditure build() {
        return new Expenditure(expenditureId, expenditureName, unitPrice, quantity, expenditureDate);
    }

    public ExpenditureBuilder withUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public ExpenditureBuilder withExpenditureDate(LocalDate expenditureDate) {
        this.expenditureDate = expenditureDate;
        return this;
    }

    public ExpenditureBuilder withExpenditureId(Integer expenditureId) {
        this.expenditureId = expenditureId;
        return this;
    }

    public ExpenditureBuilder withExpenditureName(String expenditureName) {
        this.expenditureName = expenditureName;
        return this;
    }

    public ExpenditureBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
}