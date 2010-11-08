package com.longone.broker.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AccountInfo implements IsSerializable {
    //{"初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例"};
    private double intialPrincipal;
    private double leftCapitical;
    private double stockValue;
    private double totalValue;
    private double profit;
    private double profitPct;

    public double getIntialPrincipal() {
        return intialPrincipal;
    }

    public void setIntialPrincipal(double intialPrincipal) {
        this.intialPrincipal = intialPrincipal;
    }

    public double getLeftCapitical() {
        return leftCapitical;
    }

    public void setLeftCapitical(double leftCapitical) {
        this.leftCapitical = leftCapitical;
    }

    public double getStockValue() {
        return stockValue;
    }

    public void setStockValue(double stockValue) {
        this.stockValue = stockValue;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getProfitPct() {
        return profitPct;
    }

    public void setProfitPct(double profitPct) {
        this.profitPct = profitPct;
    }
}
