package com.longone.broker.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class MockStock implements EntryPoint {
    private StockServiceAsync stockSvc = GWT.create(StockService.class);
    public static String SESSION_TIMEOUT_MSG = "会话过期，请刷新页面重新登录";

    public void onModuleLoad() {
        String value = com.google.gwt.user.client.Window.Location.getParameter("mockstock");
        if ("1".equals(value)) {
            ManagePanel managePanel = new ManagePanel(stockSvc);
            SESSION_TIMEOUT_MSG = "会话过期，请重新登录OA，点击链接打开页面";
            RootPanel.get("tab").add(managePanel);
            managePanel.initialize();
        } else {
            Panel loginPanel = new LoginPanel(stockSvc);
            RootPanel.get("login").add(loginPanel);
        }
    }
}