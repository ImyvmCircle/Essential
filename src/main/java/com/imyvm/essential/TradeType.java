package com.imyvm.essential;

import com.imyvm.economy.api.TradeTypeEnum;

public enum TradeType implements TradeTypeEnum.TradeTypeExtension {
    BONUS,
    FLY,
    DEATH_PROTECT;

    private double tax;

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public double getTax() {
        return this.tax;
    }
}
