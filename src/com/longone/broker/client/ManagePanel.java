package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class ManagePanel extends VerticalPanel implements Initializable {

    private static final String[] HEADERS = {"用户", "初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例", "股票持仓比例", " ", " "};
    private static final String[] TRANS_HISTORY_HEADERS = {"股票代码", "股票简称", "买卖方向", "成交价格", "数量", "佣金", "交易时间"};
    private static final String[] POSITION_HEADERS = {"代码", "名称", "数量", "成本价格", "产生佣金", "股票现价",
            "浮动盈亏", "浮动盈亏比例", "个股市值", "个股持仓比例"};

    private Button refreshBtn = new Button("刷新");
    private StockServiceAsync stockSvc;
    private Grid rankGrid = new Grid();
    private Grid transGrid = null;
    private Grid positionGrid = null;
    private Label transUser = null;

    public ManagePanel(StockServiceAsync stockSvc) {
        setSpacing(6);
        this.stockSvc = stockSvc;
        this.add(refreshBtn);
        this.add(rankGrid);
        refreshBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                removeGrids();
                refreshBtn.setEnabled(false);
                loadData();
            }
        });
        rankGrid.resize(1, HEADERS.length);
        Util.createGridHeader(rankGrid, HEADERS);
    }

    

    private void loadData() {
        // Set up the callback object.
        AsyncCallback<AccountInfo[]> callback = new AsyncCallback<AccountInfo[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                refreshBtn.setEnabled(true);
            }

            public void onSuccess(AccountInfo[] info) {
                if (info == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                populateRankTable(info);
                refreshBtn.setEnabled(true);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getAllAccountInfo(callback);
    }

    private void populateRankTable(AccountInfo[] info) {
        Util.removeGridData(rankGrid);
        rankGrid.resizeRows(info.length+1);
        for (int row = 1; row <= info.length; row++) {
            insertRankTableRow(row, info[row - 1]);
        }
    }

    private void insertRankTableRow(int row, final AccountInfo info) {
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        rankGrid.setWidget(row, 0, new Label(info.getDisplayName()));
        rankGrid.getCellFormatter().addStyleName(row, 0, "textCell");            
        rankGrid.setWidget(row, 1, new Label(fmt.format(info.getIntialPrincipal())));
        rankGrid.getCellFormatter().addStyleName(row, 1, "numericCell");
        rankGrid.setWidget(row, 2, new Label(fmt.format(info.getLeftCapitical())));
        rankGrid.getCellFormatter().addStyleName(row, 2, "numericCell");
        rankGrid.setWidget(row, 3, new Label(fmt.format(info.getStockValue())));
        rankGrid.getCellFormatter().addStyleName(row, 3, "numericCell");
        rankGrid.setWidget(row, 4, new Label(fmt.format(info.getTotalValue())));
        rankGrid.getCellFormatter().addStyleName(row, 4, "numericCell");
        rankGrid.setWidget(row, 5, new Label(fmt.format(info.getProfit())));
        rankGrid.getCellFormatter().addStyleName(row, 5, "numericCell");
        rankGrid.setWidget(row, 6, new Label(fmt.format(info.getProfitPct()) + "%"));
        rankGrid.getCellFormatter().addStyleName(row, 6, "numericCell");
        if (info.getProfit() > 0) {

            rankGrid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            rankGrid.getCellFormatter().addStyleName(row, 5, "positiveChange");
            rankGrid.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            rankGrid.getCellFormatter().addStyleName(row, 6, "positiveChange");
        } else if (info.getProfit() < 0) {
            rankGrid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            rankGrid.getCellFormatter().addStyleName(row, 5, "negativeChange");
            rankGrid.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            rankGrid.getCellFormatter().addStyleName(row, 6, "negativeChange");
        } else {
            rankGrid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            rankGrid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            rankGrid.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            rankGrid.getCellFormatter().removeStyleName(row, 6, "negativeChange");
        }

        if (info.getTotalValue() != 0) {
            rankGrid.setWidget(row, 7, new Label(fmt.format(100.0 * info.getStockValue() / info.getTotalValue()) + "%"));
        } else {
            rankGrid.setWidget(row, 7, new Label("--"));
        }
        rankGrid.getCellFormatter().addStyleName(row, 7, "numericCell");

        Button button = new Button("交易记录");
        rankGrid.setWidget(row, 8, button);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showTransHistory(info.getUsername(), info.getDisplayName());
            }
        });

        Button positionBtn = new Button("当前持仓");
        rankGrid.setWidget(row, 9, positionBtn);
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
        positionGrid = new Grid(positions.length+1, POSITION_HEADERS.length);
        // Create table for stock Positions.
        Util.createGridHeader(positionGrid, POSITION_HEADERS);
        positionGrid.setCellPadding(3);

        for (int i = 0; i < positions.length; i++) {
            Util.addPositionRow(positionGrid, positions[i], i + 1);
        }
    }


    private void createTransTable(DealLog[] logs) {
        transGrid = new Grid(logs.length + 1, TRANS_HISTORY_HEADERS.length);

        Util.createGridHeader(transGrid, TRANS_HISTORY_HEADERS);
        transGrid.setCellPadding(3);

        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        for (int row = 1; row <= logs.length; row++) {
            final DealLog deal = logs[row - 1];
            transGrid.setWidget(row, 0, new Label(deal.getCode()));
            transGrid.getCellFormatter().addStyleName(row, 0, "textCell");
            transGrid.setWidget(row, 1, new Label(deal.getName()));
            transGrid.getCellFormatter().addStyleName(row, 1, "textCell");
            transGrid.setWidget(row, 2, new Label(deal.getBs()));
            transGrid.getCellFormatter().addStyleName(row, 2, "textCell");
            if ("买入".equals(deal.getBs())) {
                transGrid.getCellFormatter().removeStyleName(row, 2, "negativeChange");
                transGrid.getCellFormatter().addStyleName(row, 2, "positiveChange");
            } else {
                transGrid.getCellFormatter().removeStyleName(row, 2, "positiveChange");
                transGrid.getCellFormatter().addStyleName(row, 2, "negativeChange");
            }
            transGrid.setWidget(row, 3, new Label(fmt.format(deal.getPrice())));
            transGrid.getCellFormatter().addStyleName(row, 3, "numericCell");
            transGrid.setWidget(row, 4, new Label(fmt.format(deal.getAmount())));
            transGrid.getCellFormatter().addStyleName(row, 4, "numericCell");
            transGrid.setWidget(row, 5, new Label(fmt.format(deal.getCommission())));
            transGrid.getCellFormatter().addStyleName(row, 5, "numericCell");
            transGrid.setWidget(row, 6, new Label(deal.getCreated()));
        }
    }


    public void initialize() {
        loadData();
    }

    public VerticalPanel getItself() {
        return this;
    }
}
