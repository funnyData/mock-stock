package com.longone.broker.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;


public final class Util {
    public static void addPositionRow(FlexTable table, StockPosition position, int row) {
        table.getRowFormatter().addStyleName(row, "watchListNumericColumn");
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00;#,##0.00");
        table.setText(row, 0, position.getCode());
        table.setText(row, 1, position.getName());
        table.setText(row, 2, String.valueOf(position.getAmount()));
        table.setText(row, 3, fmt.format(position.getCostPrice()));
        table.setText(row, 4, fmt.format(position.getCommission()));
        table.setText(row, 5, fmt.format(position.getCurrentPrice()));
        if (position.getCurrentPrice() > position.getCostPrice()) {
            table.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            table.getCellFormatter().addStyleName(row, 5, "positiveChange");
        } else {
            table.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            table.getCellFormatter().addStyleName(row, 5, "negativeChange");
        }

        table.setText(row, 8, fmt.format(position.getStockValue()));
        table.setText(row, 9, fmt.format(position.getStockValuePct()) + "%");

        fmt = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        table.setText(row, 6, fmt.format(position.getProfit()));
        table.setText(row, 7, String.valueOf(position.getProfitPct()) + "%");
        if (position.getProfit() > 0) {
            table.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            table.getCellFormatter().addStyleName(row, 6, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 7, "negativeChange");
            table.getCellFormatter().addStyleName(row, 7, "positiveChange");
        } else if(position.getProfit() < 0) {
            table.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            table.getCellFormatter().addStyleName(row, 6, "negativeChange");
            table.getCellFormatter().removeStyleName(row, 7, "positiveChange");
            table.getCellFormatter().addStyleName(row, 7, "negativeChange");
        }
        else {
            table.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            table.getCellFormatter().removeStyleName(row, 7, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 7, "negativeChange");
        }
    }
}
