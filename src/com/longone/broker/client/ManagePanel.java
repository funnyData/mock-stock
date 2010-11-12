package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class ManagePanel extends VerticalPanel implements Initializable {
    private FlexTable grid = new FlexTable();
    private static final String[] HEADERS = {"用户", "初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例", "股票持仓比例", " ", " "};
    private static final String[] TRANS_HISTORY_HEADERS = {"股票代码", "股票简称", "买卖方向", "成交价格", "数量", "佣金", "交易时间"};
    private static final String[] POSITION_HEADERS = {"代码", "名称", "数量", "成本价格", "产生佣金", "股票现价",
            "浮动盈亏", "浮动盈亏比例", "个股市值", "个股持仓比例"};

    private Button button = new Button("刷新");
    private StockServiceAsync stockSvc;
    private Grid transGrid = null;
    private FlexTable positionGrid = null;

    private Label transUser = null;

    public ManagePanel(StockServiceAsync stockSvc) {
        this.stockSvc = stockSvc;
        this.add(button);
        this.add(grid);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                removeGrids();
                button.setEnabled(false);
                loadData();
            }
        });
        createGridHeader();
    }

    private void createGridHeader() {
        for (int i = 0; i < HEADERS.length; i++) {
            grid.setWidget(0, i, new Label(HEADERS[i]));
        }
        grid.getRowFormatter().setStyleName(0, "watchListHeader");
        grid.addStyleName("watchList");
        grid.setCellPadding(6);
    }

    private void loadData() {
        // Set up the callback object.
        AsyncCallback<AccountInfo[]> callback = new AsyncCallback<AccountInfo[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                button.setEnabled(true);
            }

            public void onSuccess(AccountInfo[] info) {
                if (info == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                populateGridData(info);
                button.setEnabled(true);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getAllAccountInfo(callback);
    }

    private void populateGridData(AccountInfo[] info) {
        for (int row = 1; row <= info.length; row++) {
            populateGridData(row, info[row - 1]);
        }
    }

    private void populateGridData(int row, final AccountInfo info) {
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        grid.setWidget(row, 0, new Label(info.getDisplayName()));
        grid.setWidget(row, 1, new Label(fmt.format(info.getIntialPrincipal())));
        grid.setWidget(row, 2, new Label(fmt.format(info.getLeftCapitical())));
        grid.setWidget(row, 3, new Label(fmt.format(info.getStockValue())));
        grid.setWidget(row, 4, new Label(fmt.format(info.getTotalValue())));
        grid.setWidget(row, 5, new Label(fmt.format(info.getProfit())));
        grid.setWidget(row, 6, new Label(fmt.format(info.getProfitPct()) + "%"));
        if (info.getProfit() > 0) {
            grid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().addStyleName(row, 5, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            grid.getCellFormatter().addStyleName(row, 6, "positiveChange");
        } else if (info.getProfit() < 0) {
            grid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            grid.getCellFormatter().addStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            grid.getCellFormatter().addStyleName(row, 6, "negativeChange");
        } else {
            grid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 6, "negativeChange");
        }

        if (info.getTotalValue() != 0) {
            grid.setWidget(row, 7, new Label(fmt.format(100.0 * info.getStockValue() / info.getTotalValue()) + "%"));
        } else {
            grid.setWidget(row, 7, new Label("--"));
        }

        Button button = new Button("交易记录");
        grid.setWidget(row, 8, button);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showTransHistory(info.getUsername(), info.getDisplayName());
            }
        });

        Button positionBtn = new Button("当前持仓");
        grid.setWidget(row, 9, positionBtn);
        positionBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showPosition(info.getUsername(), info.getDisplayName());
            }
        });
    }

    private void showPosition(final String username, final String displayName) {
        AsyncCallback<StockPosition[]> callback = new AsyncCallback<StockPosition[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
            }

            public void onSuccess(StockPosition[] positions) {
                if (positions == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                removeGrids();
                createPositionTable(positions);
                transUser = new Label("\"" + displayName + "\"持仓情况如下：");
                getItself().add(transUser);
                getItself().add(positionGrid);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getStockPositions(username, callback);
    }

    private void removeGrids() {
        if (transUser != null) {
            getItself().remove(transUser);
        }
        if (positionGrid != null) {
            getItself().remove(positionGrid);
        }
        if (transGrid != null) {
            getItself().remove(transGrid);
        }
    }


    private void showTransHistory(final String username, final String displayName) {
        AsyncCallback<DealLog[]> callback = new AsyncCallback<DealLog[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
            }
            public void onSuccess(DealLog[] logs) {
                if (logs == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                removeGrids();
                createTransTable(logs);
                transUser = new Label("\"" + displayName + "\"的交易记录如下：");
                getItself().add(transUser);
                getItself().add(transGrid);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getDealLogs(username, callback);
    }

    private void createPositionTable(StockPosition[] positions) {
        positionGrid = new FlexTable();
        // Create table for stock Positions.
        for (int i = 0; i < POSITION_HEADERS.length; i++) {
            positionGrid.setText(0, i, POSITION_HEADERS[i]);
            positionGrid.getCellFormatter().addStyleName(0, i, "watchListNumericColumn");
        }

        // Add styles to elements in the stock list table.
        positionGrid.getRowFormatter().addStyleName(0, "watchListHeader");
        positionGrid.addStyleName("watchList");

        for (int i = 0; i < positions.length; i++) {
            Util.addPositionRow(positionGrid, positions[i], i + 1);
        }
    }


    private void createTransTable(DealLog[] logs) {
        transGrid = new Grid(logs.length + 1, TRANS_HISTORY_HEADERS.length);
        transGrid.setCellPadding(6);
        for (int i = 0; i < TRANS_HISTORY_HEADERS.length; i++) {
            transGrid.setWidget(0, i, new Label(TRANS_HISTORY_HEADERS[i]));
        }
        transGrid.getRowFormatter().setStyleName(0, "watchListHeader");
        transGrid.addStyleName("watchList");

        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        for (int row = 1; row <= logs.length; row++) {
            final DealLog deal = logs[row - 1];
            transGrid.setWidget(row, 0, new Label(deal.getCode()));
            transGrid.setWidget(row, 1, new Label(deal.getName()));
            transGrid.setWidget(row, 2, new Label(deal.getBs()));
            if ("买入".equals(deal.getBs())) {
                transGrid.getCellFormatter().removeStyleName(row, 2, "negativeChange");
                transGrid.getCellFormatter().addStyleName(row, 2, "positiveChange");
            } else {
                transGrid.getCellFormatter().removeStyleName(row, 2, "positiveChange");
                transGrid.getCellFormatter().addStyleName(row, 2, "negativeChange");
            }
            transGrid.setWidget(row, 3, new Label(fmt.format(deal.getPrice())));
            transGrid.setWidget(row, 4, new Label(fmt.format(deal.getAmount())));
            transGrid.setWidget(row, 5, new Label(fmt.format(deal.getCommission())));
            transGrid.setWidget(row, 6, new Label(deal.getCreated()));
            transGrid.getRowFormatter().addStyleName(row, "watchListNumericColumn");
        }
    }


    public void initialize() {
        loadData();
    }

    public VerticalPanel getItself() {
        return this;
    }
}
