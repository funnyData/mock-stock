package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

public class PositionPanel extends VerticalPanel implements Initializable {
    private static final String[] TABLE_COLUMN = {"代码", "名称", "数量", "成本价格", "产生佣金", "股票现价",
            "浮动盈亏", "浮动盈亏比例", "个股市值", "个股持仓比例"};

    private Grid stocksFlexTable = null;

    private TextBox symbolBox = new TextBox();
    private TextBox amountBox = new TextBox();

    private Button buyBtn = new Button("买入");
    private Button sellBtn = new Button("卖出");
    private Button refreshBtn = new Button("刷新");
    private Label lastUpdatedLabel = new Label();

    private Timer refreshTimer;
    private static final int REFRESH_INTERVAL = 15000; // ms

    private StockServiceAsync stockSvc;

    public PositionPanel(StockServiceAsync stockSvc) {
        this.stockSvc = stockSvc;
        stocksFlexTable = new Grid(1, TABLE_COLUMN.length);
        Util.createGridHeader(stocksFlexTable, TABLE_COLUMN);

        // Assemble Add Stock panel.
        HorizontalPanel operatePanel = new HorizontalPanel();
        operatePanel.setSpacing(10);

        symbolBox.setMaxLength(6);
        symbolBox.setWidth("60px");
        amountBox.setMaxLength(10);
        amountBox.setWidth("80px");
        operatePanel.add(buyBtn);
        operatePanel.add(new Label("代码:"));
        operatePanel.add(symbolBox);
        operatePanel.add(new Label("数量:"));
        operatePanel.add(amountBox);
        operatePanel.add(sellBtn);
        operatePanel.addStyleName("addPanel");

        // Assemble Main panel.
        refreshBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                refreshPositionList();
            }
        });
        this.add(refreshBtn);
        this.add(stocksFlexTable);
        this.add(operatePanel);
        this.add(lastUpdatedLabel);
        //refreshPositionList();

        // Setup timer to refresh list automatically.
        refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshPositionList();
            }
        };


        //Listen for mouse events on the buy/sell button
        buyBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dealStock(true);
            }
        });

        sellBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dealStock(false);
            }
        });
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
        buyBtn.setEnabled(false);
        sellBtn.setEnabled(false);
        // Set up the callback object.
        AsyncCallback<String> callback = new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                buyBtn.setEnabled(true);
                sellBtn.setEnabled(true);
            }

            public void onSuccess(String result) {
                if (result == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    buyBtn.setEnabled(true);
                    sellBtn.setEnabled(true);
                    return;
                }
                refreshPositionList();
                Window.alert(result);
                buyBtn.setEnabled(true);
                sellBtn.setEnabled(true);
                symbolBox.setText("");
                symbolBox.setFocus(true);
                amountBox.setText("");
            }
        };
        // Make the call to the stock price service.
        stockSvc.placeOrder(symbol, Integer.parseInt(amount), isBuy, callback);
    }

    private void refreshPositionList() {
        refreshBtn.setEnabled(false);
        // Initialize the service proxy.
        if (stockSvc == null) {
            stockSvc = GWT.create(StockService.class);
        }

        // Set up the callback object.
        AsyncCallback<StockPosition[]> callback = new AsyncCallback<StockPosition[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                refreshBtn.setEnabled(true);
            }

            public void onSuccess(StockPosition[] result) {
                if (result == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    refreshBtn.setEnabled(true);
                    return;
                }
                updateTable(result);
                refreshBtn.setEnabled(true);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getStockPositions(callback);
    }

    private void updateTable(StockPosition[] positions) {
        Util.removeGridData(stocksFlexTable);
        stocksFlexTable.resizeRows(positions.length+1);

        for (int i = 0; i < positions.length; i++) {
            Util.addPositionRow(stocksFlexTable, positions[i], i + 1);
        }
        // Display timestamp showing last refresh.
        lastUpdatedLabel.setText("最后更新时间 : "
                + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601).format(new Date()));
    }

    public void initialize() {
        refreshPositionList();
        //refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
    }
}
