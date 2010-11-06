package com.longone.broker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.longone.broker.client.StockPrice;
import com.longone.broker.client.StockPriceService;
import java.util.Map;

public class StockPriceServiceImpl extends RemoteServiceServlet implements StockPriceService {
    public StockPrice[] getPrices(String[] symbols) {
        Map<String, StockPrice> data = DBFReaderThread.getData();

        StockPrice[] prices = new StockPrice[symbols.length];
        for(int i=0; i<symbols.length; i++){
            prices[i] = data.get(symbols[i]);
        }
        return prices;
    }
}
