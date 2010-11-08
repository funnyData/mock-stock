package com.longone.broker.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class MockStock implements EntryPoint {
    private StockServiceAsync stockSvc = GWT.create(StockService.class);
    public static final String SESSION_TIMEOUT_MSG = "会话过期，请刷新页面重新登录";

    public void onModuleLoad() {
        Panel loginPanel = new LoginPanel(stockSvc);
        RootPanel.get("stockList").add(loginPanel);
    }
}