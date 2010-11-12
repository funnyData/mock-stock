package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;


public class TransHistoryPanel extends VerticalPanel implements Initializable{
    private Grid grid = new Grid();
    private Button button = new Button("刷新");
    private static String[] HEADERS = {"股票代码", "股票简称", "买卖方向", "成交价格", "数量", "佣金", "交易时间"};
    private StockServiceAsync stockSvc;

    public TransHistoryPanel(StockServiceAsync stockSvc) {
        this.stockSvc = stockSvc;
        this.add(button);
        this.add(grid);
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);
                loadData();
            }
        });
        grid.setCellPadding(6);
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

        grid.resize(logs.length + 1, 7);
        for (int i = 0; i < HEADERS.length; i++) {
            grid.setWidget(0, i, new Label(HEADERS[i]));
        }
        grid.getRowFormatter().setStyleName(0, "watchListHeader");
        grid.addStyleName("watchList");

        NumberFormat fmt = NumberFormat.getFormat("#,##0.00");
        for(int row=1; row<=logs.length;row++) {
            DealLog deal = logs[row-1];
            grid.setWidget(row, 0, new Label(deal.getCode()));
            grid.setWidget(row, 1, new Label(deal.getName()));
            grid.setWidget(row, 2, new Label(deal.getBs()));
            if("买入".equals(deal.getBs())) {
                grid.getCellFormatter().removeStyleName(row, 2, "negativeChange");
                grid.getCellFormatter().addStyleName(row, 2, "positiveChange");
            }
            else {
                grid.getCellFormatter().removeStyleName(row, 2, "positiveChange");
                grid.getCellFormatter().addStyleName(row, 2, "negativeChange");
            }
            grid.setWidget(row, 3, new Label(fmt.format(deal.getPrice())));
            grid.setWidget(row, 4, new Label(fmt.format(deal.getAmount())));
            grid.setWidget(row, 5, new Label(fmt.format(deal.getCommission())));
            grid.setWidget(row, 6, new Label(deal.getCreated()));
            grid.getRowFormatter().addStyleName(row, "watchListNumericColumn");
        }
    }


    public void initialize() {
        loadData();
    }
}
