package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AccountPanel extends VerticalPanel implements Initializable {

    private static final String[] HEADERS = {"初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例", "股票持仓比例"};
    private Grid grid = null;
    private Button button = new Button("刷新");
    private StockServiceAsync stockSvc;

    public AccountPanel(StockServiceAsync stockSvc) {
        grid = new Grid(2, HEADERS.length);
        Util.createGridHeader(grid, HEADERS);
        this.stockSvc = stockSvc;
        this.add(button);
        this.add(grid);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);
                loadData();
            }
        });
    }

    private void loadData() {
        // Set up the callback object.
        AsyncCallback<AccountInfo> callback = new AsyncCallback<AccountInfo>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                button.setEnabled(true);
            }

            public void onSuccess(AccountInfo info) {
                if (info == null) {
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                populateGridData(info);
                button.setEnabled(true);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getAccountInfo(callback);
    }

    private void populateGridData(AccountInfo info) {
        Util.removeGridData(grid);
        grid.resizeRows(2);
        int row = 1;
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        grid.setWidget(row, 0, new Label(fmt.format(info.getIntialPrincipal())));
        grid.getCellFormatter().addStyleName(row, 0, "numericCell");
        grid.setWidget(row, 1, new Label(fmt.format(info.getLeftCapitical())));
        grid.getCellFormatter().addStyleName(row, 1, "numericCell");
        grid.setWidget(row, 2, new Label(fmt.format(info.getStockValue())));
        grid.getCellFormatter().addStyleName(row, 2, "numericCell");
        grid.setWidget(row, 3, new Label(fmt.format(info.getTotalValue())));
        grid.getCellFormatter().addStyleName(row, 3, "numericCell");
        grid.setWidget(row, 4, new Label(fmt.format(info.getProfit())));
        grid.getCellFormatter().addStyleName(row, 4, "numericCell");
        grid.setWidget(row, 5, new Label(fmt.format(info.getProfitPct()) + "%"));
        grid.getCellFormatter().addStyleName(row, 5, "numericCell");
        if (info.getProfit() > 0) {

            grid.getCellFormatter().removeStyleName(row, 4, "negativeChange");
            grid.getCellFormatter().addStyleName(row, 4, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().addStyleName(row, 5, "positiveChange");
        } else if (info.getProfit() < 0) {
            grid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            grid.getCellFormatter().addStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().removeStyleName(row, 4, "positiveChange");
            grid.getCellFormatter().addStyleName(row, 4, "negativeChange");
        } else {
            grid.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            grid.getCellFormatter().removeStyleName(row, 4, "positiveChange");
            grid.getCellFormatter().removeStyleName(row, 4, "negativeChange");
        }

        if (info.getTotalValue() != 0) {
            grid.setWidget(row, 6, new Label(fmt.format(100.0 * info.getStockValue() / info.getTotalValue()) + "%"));
        } else {
            grid.setWidget(row, 6, new Label("--"));
        }
        grid.getCellFormatter().addStyleName(row, 6, "numericCell");
    }


    public void initialize() {
        loadData();
    }
}
