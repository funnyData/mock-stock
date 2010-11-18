package com.longone.broker.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;


public final class Util {
    public static void addPositionRow(Grid table, StockPosition position, int row) {
        NumberFormat fmt = NumberFormat.getFormat("#,##0.00;#,##0.00");
        table.setText(row, 0, position.getCode());
        table.setText(row, 1, position.getName());
        table.setText(row, 2, String.valueOf(position.getAmount()));
        table.getCellFormatter().setStyleName(row, 2, "numericCell");
        table.setText(row, 3, fmt.format(position.getCostPrice()));
        table.getCellFormatter().setStyleName(row, 3, "numericCell");
        table.setText(row, 4, fmt.format(position.getCommission()));
        table.getCellFormatter().setStyleName(row, 4, "numericCell");
        table.setText(row, 5, fmt.format(position.getCurrentPrice()));
        table.getCellFormatter().setStyleName(row, 5, "numericCell");
        if (position.getCurrentPrice() > position.getCostPrice()) {
            table.getCellFormatter().removeStyleName(row, 5, "negativeChange");
            table.getCellFormatter().addStyleName(row, 5, "positiveChange");
        } else {
            table.getCellFormatter().removeStyleName(row, 5, "positiveChange");
            table.getCellFormatter().addStyleName(row, 5, "negativeChange");
        }

        table.setText(row, 8, fmt.format(position.getStockValue()));
        table.getCellFormatter().setStyleName(row, 8, "numericCell");
        table.setText(row, 9, fmt.format(position.getStockValuePct()) + "%");
        table.getCellFormatter().setStyleName(row, 9, "numericCell");

        fmt = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        table.setText(row, 6, fmt.format(position.getProfit()));
        table.getCellFormatter().setStyleName(row, 6, "numericCell");
        table.setText(row, 7, String.valueOf(position.getProfitPct()) + "%");
        table.getCellFormatter().setStyleName(row, 7, "numericCell");
        if (position.getProfit() > 0) {
            table.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            table.getCellFormatter().addStyleName(row, 6, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 7, "negativeChange");
            table.getCellFormatter().addStyleName(row, 7, "positiveChange");
        } else if (position.getProfit() < 0) {
            table.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            table.getCellFormatter().addStyleName(row, 6, "negativeChange");
            table.getCellFormatter().removeStyleName(row, 7, "positiveChange");
            table.getCellFormatter().addStyleName(row, 7, "negativeChange");
        } else {
            table.getCellFormatter().removeStyleName(row, 6, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 6, "negativeChange");
            table.getCellFormatter().removeStyleName(row, 7, "positiveChange");
            table.getCellFormatter().removeStyleName(row, 7, "negativeChange");
        }
    }

    public static void createGridHeader(Grid grid, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            grid.setWidget(0, i, new Label(headers[i]));
        }
        grid.getRowFormatter().addStyleName(0, "tableHeader");
        grid.addStyleName("tableList");
        grid.setCellPadding(4);
    }

    public static void removeGridData(Grid grid) {
        int row = grid.getRowCount();
        for (int i = 1; i < row; i++) {
            grid.removeRow(1);
        }
    }
}
