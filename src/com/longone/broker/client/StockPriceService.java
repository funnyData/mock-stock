package com.longone.broker.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.longone.broker.client.StockPrice;

@RemoteServiceRelativePath("stockPrices")
public interface StockPriceService extends RemoteService {
    StockPrice[] getPrices(String[] symbols);
}
