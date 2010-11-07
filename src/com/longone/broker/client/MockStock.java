package com.longone.broker.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

public class MockStock implements EntryPoint {
    private static final String[] TABLE_COLUMN = {"代码", "名称", "数量", "成本价格", "产生佣金", "股票现价",
            "浮动盈亏"};

    private FlexTable stocksFlexTable = new FlexTable();

    private TextBox symbolBox = new TextBox();
    private TextBox amountBox = new TextBox();

    private Button buyStockButton = new Button("买入");
    private Button sellStockButton = new Button("卖出");
    private Label lastUpdatedLabel = new Label();

    private static final int REFRESH_INTERVAL = 15000; // ms
    private StockServiceAsync stockPriceSvc = GWT.create(StockService.class);

    /**
     * Entry point method.
     */
    public void onModuleLoad() {
        DecoratedTabPanel tabPanel = new DecoratedTabPanel();
        tabPanel.setWidth("1000px");
        tabPanel.setAnimationEnabled(true);
        tabPanel.add(createPositionPanel(), "持仓");
        tabPanel.add(new ProfitPanel(stockPriceSvc), "成交记录");
        tabPanel.add(new PasswordResetPanel(stockPriceSvc), "修改密码");
        tabPanel.selectTab(0);

        // Associate the Main panel with the HTML host page.
        RootPanel.get("stockList").add(tabPanel);
    }

    private Panel createPositionPanel() {
        VerticalPanel mainPanel = new VerticalPanel();

        // Create table for stock Positions.
        for (int i = 0; i < TABLE_COLUMN.length; i++) {
            stocksFlexTable.setText(0, i, TABLE_COLUMN[i]);
            stocksFlexTable.getCellFormatter().addStyleName(0, i, "watchListNumericColumn");
        }

        // Add styles to elements in the stock list table.
        stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
        stocksFlexTable.addStyleName("watchList");

        // Assemble Add Stock panel.
        HorizontalPanel operatePanel = new HorizontalPanel();

        operatePanel.add(new Label("代码:"));
        operatePanel.add(symbolBox);
        operatePanel.add(new Label("数量:"));
        operatePanel.add(amountBox);
        operatePanel.add(buyStockButton);
        operatePanel.add(sellStockButton);
        operatePanel.addStyleName("addPanel");

        // Assemble Main panel.
        mainPanel.add(stocksFlexTable);
        mainPanel.add(operatePanel);
        mainPanel.add(lastUpdatedLabel);
        refreshPositionList();

        // Setup timer to refresh list automatically.
        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshPositionList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

        //Listen for mouse events on the buy/sell button
        buyStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dealStock(true);
            }
        });

        sellStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dealStock(false);
            }
        });


        return mainPanel;
    }

    private void dealStock(boolean isBuy) {

        final String symbol = symbolBox.getText().toUpperCase().trim();
        final String amount = amountBox.getText().trim();

        String error = null;
        if (!symbol.matches("^[0-9]{6}$")) {
            error = "请输入6位股票代码";
            symbolBox.setText("");
            symbolBox.setFocus(true);
        } else if (!amount.matches("^[0-9]+$")) {
            error = "请输入正确的数量";
            amountBox.setText("");
            amountBox.setFocus(true);
        } else {
            int value = Integer.parseInt(amount);
            if (value % 100 != 0) {
                error = "数量必须是100的整数倍";
                amountBox.setText("");
                amountBox.setFocus(true);
            }
        }
        if (error != null) {
            Window.alert(error);
            return;
        }

        placeOrder(symbol, amount, isBuy);
        refreshPositionList();
    }

    private void placeOrder(String symbol, String amount, boolean isBuy) {
        buyStockButton.setEnabled(false);
        sellStockButton.setEnabled(false);
        // Set up the callback object.
        AsyncCallback<String> callback = new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
            }

            public void onSuccess(String result) {
                refreshPositionList();
                Window.alert(result);
                buyStockButton.setEnabled(true);
                sellStockButton.setEnabled(true);
                symbolBox.setText("");
                symbolBox.setFocus(true);
                amountBox.setText("");
            }
        };
        // Make the call to the stock price service.
        stockPriceSvc.placeOrder(symbol, Integer.parseInt(amount), isBuy, callback);
    }

    private void refreshPositionList() {
        // Initialize the service proxy.
        if (stockPriceSvc == null) {
            stockPriceSvc = GWT.create(StockService.class);
        }

        // Set up the callback object.
        AsyncCallback<StockPosition[]> callback = new AsyncCallback<StockPosition[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
            }

            public void onSuccess(StockPosition[] result) {
                updateTable(result);
            }
        };
        // Make the call to the stock price service.
        stockPriceSvc.getStockPositions(callback);
    }

    private void updateTable(StockPosition[] positions) {
        for (int i = 0; i < positions.length; i++) {
            updateTable(positions[i], i + 1);
        }
        // Display timestamp showing last refresh.
        lastUpdatedLabel.setText("Last update : "
                + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()));

    }

    /**
     * Update a single row in the stock table.
     *
     * @param position Stock data for a single row.
     * @param row
     */
    private void updateTable(StockPosition position, int row) {
        stocksFlexTable.getRowFormatter().addStyleName(row, "watchListNumericColumn");
        GWT.log("=======================");
        GWT.log(position.getName());
        GWT.log(String.valueOf(position.getCommission()));
        GWT.log(String.valueOf(position.getCurrentPrice()));
        GWT.log(String.valueOf(position.getCostPrice()));
        GWT.log(String.valueOf(position.getProfit()));
        GWT.log("=======================");
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00;#,##0.00");
        stocksFlexTable.setText(row, 0, position.getCode());
        stocksFlexTable.setText(row, 1, position.getName());
        stocksFlexTable.setText(row, 2, String.valueOf(position.getAmount()));
        stocksFlexTable.setText(row, 3, fmt.format(position.getCostPrice()));
        stocksFlexTable.setText(row, 4, fmt.format(position.getCommission()));
        stocksFlexTable.setText(row, 5, fmt.format(position.getCurrentPrice()));
        if (position.getCurrentPrice() > position.getCostPrice()) {
            stocksFlexTable.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            stocksFlexTable.getCellFormatter().addStyleName(row, 5, "positiveChange");
        } else {
            stocksFlexTable.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            stocksFlexTable.getCellFormatter().addStyleName(row, 5, "negativeChange");
        }

        fmt = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        stocksFlexTable.setText(row, 6, fmt.format(position.getProfit()));
        if (position.getProfit() > 0) {
            stocksFlexTable.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            stocksFlexTable.getCellFormatter().addStyleName(row, 6, "positiveChange");
        } else {
            stocksFlexTable.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            stocksFlexTable.getCellFormatter().addStyleName(row, 6, "negativeChange");
        }
    }
}

