package com.longone.broker.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StockPrice implements IsSerializable {
    private String code;
    private String name;
    private double price;
    private double preClose;
    private double open;
    private double highest;
    private double lowest;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPreClose() {
        return preClose;
    }

    public void setPreClose(double preClose) {
        this.preClose = preClose;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHighest() {
        return highest;
    }

    public void setHighest(double highest) {
        this.highest = highest;
    }

    public double getLowest() {
        return lowest;
    }

    public void setLowest(double lowest) {
        this.lowest = lowest;
    }
}
