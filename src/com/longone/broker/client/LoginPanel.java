package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;


public class LoginPanel extends DecoratorPanel {
    private TextBox usernameBox = new TextBox();
    private PasswordTextBox passwordBox = new PasswordTextBox();
    private StockServiceAsync stockSvc;
    

    public LoginPanel(StockServiceAsync stockSvc) {
        this.stockSvc = stockSvc;

        // Create a table to layout the form options
        FlexTable layout = new FlexTable();
        layout.setCellSpacing(6);

        // Add some standard form options
        layout.setText(1, 0, "用户名:");
        layout.setWidget(1, 1, usernameBox);
        layout.setHTML(2, 0, "密码:");
        layout.setWidget(2, 1, passwordBox);

        Button loginBtn = new Button("登录");
        layout.setWidget(3, 1, loginBtn);

        this.setWidget(layout);

        loginBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameBox.getText().trim();
        String password = passwordBox.getText().trim();
        if ("".equals(username) || "".equals(password)) {
            Window.alert("请输入用户名和密码!!!");
            return;
        }

        AsyncCallback<User> callback = new AsyncCallback<User>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
            }

            public void onSuccess(User user) {
               if(user == null){
                   Window.alert("用户名或密码错误！！！");
                   usernameBox.setText("");
                   passwordBox.setText("");
                   return;
               }

                // change view
                RootPanel.get("stockList").remove(getItself());
                RootPanel.get("stockList").add(new Label("欢迎 "+ user.getUsername()));

                DecoratedTabPanel tabs = createTabPanel();
                RootPanel.get("stockList").add(tabs);
                tabs.selectTab(0);
            }
        };
        // Make the call to the stock price service.
        stockSvc.login(username, password, callback);
    }

    private DecoratorPanel getItself() {
        return this;
    }

    private DecoratedTabPanel createTabPanel() {
        final DecoratedTabPanel tabPanel = new DecoratedTabPanel();
        tabPanel.setWidth("1000px");
        tabPanel.setAnimationEnabled(true);
        tabPanel.add(new PositionPanel(stockSvc), "持仓");
        tabPanel.add(new TransHistoryPanel(stockSvc), "成交记录");
        tabPanel.add(new AccountPanel(stockSvc), "账户");
        tabPanel.add(new PasswordResetPanel(stockSvc), "修改密码");

        // lazy loading
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>(){
            public void onSelection(SelectionEvent selectionEvent) {
                Object obj = tabPanel.getWidget((Integer) selectionEvent.getSelectedItem());
                if(obj instanceof Initializable) {
                    ((Initializable)obj).initialize();
                }
            }
        });
        return tabPanel;
    }
}