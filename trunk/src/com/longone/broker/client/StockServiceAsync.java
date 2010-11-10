package com.longone.broker.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StockServiceAsync {
    void getStockPositions(AsyncCallback<StockPosition[]> async);
    void getStockPositions(String username, AsyncCallback<StockPosition[]> async);
    void placeOrder(String code, int amount, boolean isBuy, AsyncCallback<String> async);
    void resetPassword(String password, AsyncCallback<String> async);
    void getDealLogs(AsyncCallback<DealLog[]> async);
    void getDealLogs(String username, AsyncCallback<DealLog[]> async);
    void login(String username, String password, AsyncCallback<User> async);
    void getAccountInfo(AsyncCallback<AccountInfo> async);
    void getAllAccountInfo(AsyncCallback<AccountInfo[]> async);
}
