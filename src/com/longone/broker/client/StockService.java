package com.longone.broker.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("stockPositions")
public interface StockService extends RemoteService {
    StockPosition[] getStockPositions();
    StockPosition[] getStockPositions(String username);
    String placeOrder(String code, int amount, boolean isBuy);
    String resetPassword(String password);
    DealLog[] getDealLogs();
    DealLog[] getDealLogs(String username);
    User login(String username, String password);
    AccountInfo getAccountInfo();
    AccountInfo[] getAllAccountInfo();
}
