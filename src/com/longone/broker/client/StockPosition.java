package com.longone.broker.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StockPosition implements IsSerializable {
    private String code;
    private String name;
    private int amount;
    private double costPrice;
    private double commission;
    private double currentPrice;
    private double profit;
    private double profitPct;
    private double stockValue;
    private double stockValuePct;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
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

    public double getStockValuePct() {
        return stockValuePct;
    }

    public void setStockValuePct(double stockValuePct) {
        this.stockValuePct = stockValuePct;
    }

    public double getStockValue() {
        return stockValue;
    }

    public void setStockValue(double stockValue) {
        this.stockValue = stockValue;
    }
}
