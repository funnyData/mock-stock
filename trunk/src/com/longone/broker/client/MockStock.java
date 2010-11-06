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

import java.util.ArrayList;
import java.util.Date;

public class MockStock implements EntryPoint {
    private static final String [] TABLE_COLUMN = {"代码", "名称", "数量", "操作价格", "产生佣金", "持仓成本",
            "占用资金", "股票现价", "股票市值", "盈亏比例", "个股占比", "剩余资金", "股票市值",
            "现有市值", "组合仓位", "组合盈利"};

    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton = new Button("新增股票");
    private Label lastUpdatedLabel = new Label();

    private static final int REFRESH_INTERVAL = 5000; // ms
    private ArrayList<String> stocks = new ArrayList<String>();
    private StockPriceServiceAsync stockPriceSvc = GWT.create(StockPriceService.class);

    /**
     * Entry point method.
     */
    public void onModuleLoad() {
        createStockWatchPanel();

        DecoratedTabPanel tabPanel = new DecoratedTabPanel();
        tabPanel.setWidth("1000px");
        tabPanel.setAnimationEnabled(true);
        tabPanel.add(mainPanel, "持仓");
        tabPanel.add(new HorizontalPanel(), "修改密码");
        tabPanel.selectTab(0);

        // Associate the Main panel with the HTML host page.
        RootPanel.get("stockList").add(tabPanel);

        // Move cursor focus to the input box.
        newSymbolTextBox.setFocus(true);

        // Setup timer to refresh list automatically.
        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

        //Listen for mouse events on the ADD button
        addStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addStock();
            }
        });
    }

    private void createStockWatchPanel() {
        // Create table for stock data.
        for(int i=0; i<TABLE_COLUMN.length; i++) {
            stocksFlexTable.setText(0, i, TABLE_COLUMN[i]);
            stocksFlexTable.getCellFormatter().addStyleName(0, i, "watchListNumericColumn");
        }
        // Add styles to elements in the stock list table.
        stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
        stocksFlexTable.addStyleName("watchList");

        // Assemble Add Stock panel.
        addPanel.add(newSymbolTextBox);
        addPanel.add(addStockButton);
        addPanel.addStyleName("addPanel");

        // Assemble Main panel.
        mainPanel.add(stocksFlexTable);
        mainPanel.add(addPanel);
        mainPanel.add(lastUpdatedLabel);
    }

    private void addStock() {
        final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
        newSymbolTextBox.setFocus(true);

        // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
        if (!symbol.matches("^[0-9]{6}$")) {
            Window.alert("股票代码'" + symbol + "'不正确");
            newSymbolTextBox.selectAll();
            return;
        }

        newSymbolTextBox.setText("");

        // Add the stock to the table.
        int row = stocksFlexTable.getRowCount();
        stocks.add(symbol);
        stocksFlexTable.setText(row, 0, symbol);
        stocksFlexTable.setWidget(row, 2, new Label());
        stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

        // Add a button to remove this stock from the table.
        Button removeStockButton = new Button("x");
        removeStockButton.addStyleDependentName("remove");
        removeStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                int removedIndex = stocks.indexOf(symbol);
                stocks.remove(removedIndex);
                stocksFlexTable.removeRow(removedIndex + 1);
            }
        });
        stocksFlexTable.setWidget(row, 3, removeStockButton);

        // Get the stock price.
        refreshWatchList();
    }

    private void refreshWatchList() {
        // Initialize the service proxy.
        if (stockPriceSvc == null) {
            stockPriceSvc = GWT.create(StockPriceService.class);
        }

        // Set up the callback object.
        AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
            }
            public void onSuccess(StockPrice[] result) {
                updateTable(result);
            }
        };
        // Make the call to the stock price service.
        stockPriceSvc.getPrices(stocks.toArray(new String[0]), callback);
    }

    private void updateTable(StockPrice[] prices) {
        for (int i = 0; i < prices.length; i++) {
            updateTable(prices[i]);
        }
        // Display timestamp showing last refresh.
        lastUpdatedLabel.setText("Last update : "
                + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()));

    }

    /**
     * Update a single row in the stock table.
     *
     * @param price Stock data for a single row.
     */
    private void updateTable(StockPrice price) {
        // Make sure the stock is still in the stock table.
        if (!stocks.contains(price.getCode())) {
            return;
        }

        int row = stocks.indexOf(price.getCode()) + 1;

        // Format the data in the Price and Change fields.
        String priceText = NumberFormat.getFormat("#,##0.00").format(
                price.getPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
       // String changeText = changeFormat.format(price.getChange());
        //String changePercentText = changeFormat.format(price.getChangePercent());

        // Populate the Price and Change fields with new data.
        stocksFlexTable.setText(row, 1, priceText);
        Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
        //changeWidget.setText(changeText + " (" + changePercentText + "%)");

        // Change the color of text in the Change field based on its value.
        String changeStyleName = "noChange";
//        if (price.getChangePercent() < -0.1f) {
//            changeStyleName = "negativeChange";
//        } else if (price.getChangePercent() > 0.1f) {
//            changeStyleName = "positiveChange";
//        }

        changeWidget.setStyleName(changeStyleName);
    }
}

