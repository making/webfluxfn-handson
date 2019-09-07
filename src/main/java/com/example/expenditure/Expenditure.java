package com.example.expenditure;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

@JsonDeserialize(builder = ExpenditureBuilder.class)
public class Expenditure {

    private final Integer expenditureId;

    private final String expenditureName;

    private final int unitPrice;

    private final int quantity;

    private final LocalDate expenditureDate;

    Expenditure(Integer expenditureId, String expenditureName, int unitPrice, int quantity, LocalDate expenditureDate) {
        this.expenditureId = expenditureId;
        this.expenditureName = expenditureName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.expenditureDate = expenditureDate;
    }

    public Integer getExpenditureId() {
        return expenditureId;
    }


    public String getExpenditureName() {
        return expenditureName;
    }


    public int getUnitPrice() {
        return unitPrice;
    }


    public int getQuantity() {
        return quantity;
    }


    public LocalDate getExpenditureDate() {
        return expenditureDate;
    }


    @Override
    public String toString() {
        return "Expenditure{" +
            "expenditureId=" + expenditureId +
            ", expenditureName='" + expenditureName + '\'' +
            ", unitPrice=" + unitPrice +
            ", quantity=" + quantity +
            ", expenditureDate=" + expenditureDate +
            '}';
    }
}