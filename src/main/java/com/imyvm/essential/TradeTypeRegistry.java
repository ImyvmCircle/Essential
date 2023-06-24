package com.imyvm.essential;

import com.imyvm.economy.api.TradeTypeEnum;

public class TradeTypeRegistry {
    public enum TradeType implements TradeTypeEnum.TradeTypeExtension {
        BONUS,
        FLY,
        DEATHPROTECT;

        private Double tax;

        public void setTax(Double tax){
            this.tax = tax;
        }
        public Double getTax(){
            return tax;
        }
    }
}

