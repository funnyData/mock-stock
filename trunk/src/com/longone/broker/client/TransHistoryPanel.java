package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;


public class TransHistoryPanel extends VerticalPanel implements Initializable{
    private Grid grid = null;
    private Button button = new Button("刷新");
    private static String[] HEADERS = {"股票代码", "股票简称", "买卖方向", "成交价格", "数量", "佣金", "交易时间"};
    private StockServiceAsync stockSvc;

    public TransHistoryPanel(StockServiceAsync stockSvc) {
        grid = new Grid(1, HEADERS.length);
        Util.createGridHeader(grid, HEADERS, "tableList2");
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
        AsyncCallback<DealLog[]> callback = new AsyncCallback<DealLog[]>() {
            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                GWT.log(caught.toString());
                button.setEnabled(true);
            }

            public void onSuccess(DealLog[] logs) {
                if(logs == null){
                    Window.alert(MockStock.SESSION_TIMEOUT_MSG);
                    return;
                }
                populateGrid(logs);
                button.setEnabled(true);
            }
        };
        // Make the call to the stock price service.
        stockSvc.getDealLogs(callback);
    }

    private void populateGrid(DealLog[] logs) {
        Util.removeGridData(grid);
        grid.resize(logs.length + 1, HEADERS.length);

        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        for(int row=1; row<=logs.length;row++) {
            DealLog deal = logs[row-1];
            grid.setWidget(row, 0, new Label(deal.getCode()));
            grid.getCellFormatter().addStyleName(row, 0, "textCell");
            grid.setWidget(row, 1, new Label(deal.getName()));
            grid.getCellFormatter().addStyleName(row, 1, "textCell");
            grid.setWidget(row, 2, new Label(deal.getBs()));
            grid.getCellFormatter().addStyleName(row, 2, "textCell");
            if("买入".equals(deal.getBs())) {
                grid.getCellFormatter().removeStyleName(row, 2, "negativeChange");
                grid.getCellFormatter().addStyleName(row, 2, "positiveChange");
            }
            else {
                grid.getCellFormatter().removeStyleName(row, 2, "positiveChange");
                grid.getCellFormatter().addStyleName(row, 2, "negativeChange");
            }
            grid.setWidget(row, 3, new Label(fmt.format(deal.getPrice())));
            grid.getCellFormatter().addStyleName(row, 3, "numericCell");
            grid.setWidget(row, 4, new Label(fmt.format(deal.getAmount())));
            grid.getCellFormatter().addStyleName(row, 4, "numericCell");
            grid.setWidget(row, 5, new Label(fmt.format(deal.getCommission())));
            grid.getCellFormatter().addStyleName(row, 5, "numericCell");
            grid.setWidget(row, 6, new Label(deal.getCreated()));
            grid.getCellFormatter().addStyleName(row, 6, "textCell");
        }
    }

    public void initialize() {
        loadData();
    }
}
