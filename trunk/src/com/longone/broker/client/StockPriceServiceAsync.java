package com.longone.broker.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public interface StockPriceServiceAsync {
    void getPrices(String[] symbols, AsyncCallback<StockPrice[]> async);
}
