package com.longone.broker.server;

import com.longone.broker.client.AccountInfo;
import com.longone.broker.client.StockPosition;
import com.longone.broker.client.User;
import com.longone.broker.servlet.InitServlet;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ProfitCalculator {
   private static final Logger logger = Logger.getLogger(ProfitCalculator.class);


    //{"初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例"};
    public static AccountInfo getAccountInfo(User user) {
        AccountInfo info = new AccountInfo();
        info.setIntialPrincipal(user.getInitialPrincipal());
        info.setLeftCapitical(user.getPrincipal());

        StockPosition[] positions = queryPosition(user.getUsername());
        BigDecimal stockValue = new BigDecimal(0);
        for(StockPosition position :positions) {
            stockValue = BigDecimal.valueOf(position.getAmount()).multiply(BigDecimal.valueOf(position.getCurrentPrice())).setScale(2, BigDecimal.ROUND_HALF_UP).add(stockValue);
        }
        info.setStockValue(stockValue.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        info.setTotalValue(stockValue.add(BigDecimal.valueOf(user.getPrincipal())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        info.setProfit(info.getTotalValue() - info.getIntialPrincipal());
        double pct = BigDecimal.valueOf(info.getProfit()*100).divide(BigDecimal.valueOf(info.getIntialPrincipal())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        info.setProfitPct(pct);
        return info;
    }


    public static StockPosition[] queryPosition(String username) {
        String sql = "select * from positions where amount > 0 and userid = '" + username + "' order by id desc";
        DbManager manager = InitServlet.getManager();
        List<StockPosition> list = new ArrayList<StockPosition>();
        try {
            ResultSet set = manager.query(sql);
            while (set.next()) {
                StockPosition position = new StockPosition();
                String code = set.getString("code");
                position.setCode(code);
                position.setName(set.getString("name"));
                int amount = set.getInt("amount");
                position.setAmount(amount);
                position.setCommission(set.getDouble("commission"));
                double cost = set.getDouble("cost");
                position.setCostPrice(cost);

                StockPrice stockPrice = DBFReaderThread.getData().get(code);
                position.setCurrentPrice(stockPrice.getPrice());

                // calculate profit
                // (currentPrice-cost)*amount
                BigDecimal profit = BigDecimal.valueOf(stockPrice.getPrice()).subtract(BigDecimal.valueOf(cost)).multiply(BigDecimal.valueOf(amount));
                position.setProfit(profit.doubleValue());

                // calculate profitPct
                // (current-cost)*100/cost
                BigDecimal profitPct = BigDecimal.valueOf(stockPrice.getPrice()).subtract(BigDecimal.valueOf(cost))
                        .divide(BigDecimal.valueOf(cost), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
                position.setProfitPct(profitPct.doubleValue());

                logger.debug(position.getCurrentPrice() + ", " + position.getCostPrice() + ", " + position.getCode() + ", " + position.getProfit());
                list.add(position);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        StockPosition[] positions = new StockPosition[list.size()];
        for (int i = 0; i < list.size(); i++) {
            positions[i] = list.get(i);
        }
        return positions;
    }
}
