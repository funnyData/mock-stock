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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class ProfitCalculator {
    private static final Logger logger = Logger.getLogger(ProfitCalculator.class);

    public static AccountInfo[] getAllAccountInfo() {
        List<User> list = getAllUsers();
        List<AccountInfo> accounts = new ArrayList<AccountInfo>();
        for(User user : list){
            accounts.add(getAccountInfo(user));
        }
        AccountInfo [] result = new AccountInfo[accounts.size()];
        for(int i=0; i<result.length;i++) {
            result[i] = accounts.get(i);
        }
        Arrays.sort(result, new Comparator<AccountInfo>(){
            public int compare(AccountInfo o1, AccountInfo o2) {
                if(o1.getProfit() > o2.getProfit()) {
                    return -1;
                }
                else if( o1.getProfit() < o2.getProfit()) {
                    return 1;
                }
                else {
                    return 0;    
                }
            }
        });
        return result;
    }

    private static List<User> getAllUsers() {
        String sql = "select * from users";
        List<User> list = new ArrayList<User>();
        DbManager manager = InitServlet.getManager();
        ResultSet set;
        try {
            set = manager.query(sql);
            while(set.next()) {
                list.add(createUser(set));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return list;
    }

    public static User createUser(ResultSet set) throws SQLException {
        User user = new User();
        user.setUsername(set.getString("username"));
        user.setSuperUser(set.getString("superuser"));
        user.setStartDate(set.getDate("startDate"));
        user.setEndDate(set.getDate("endDate"));
        user.setPrincipal(set.getDouble("principal"));
        user.setInitialPrincipal(set.getDouble("initialPrincipal"));
        return user;
    }

    //{"初始资金", "可用资金", "股票市值", "总市值", "盈亏总额", "盈亏比例"};
    public static AccountInfo getAccountInfo(User user) {
        AccountInfo info = new AccountInfo();
        info.setUsername(user.getUsername());
        info.setIntialPrincipal(user.getInitialPrincipal());
        info.setLeftCapitical(user.getPrincipal());

        StockPosition[] positions = queryPosition(user.getUsername());
        BigDecimal stockValue = new BigDecimal(0);
        for (StockPosition position : positions) {
            stockValue = BigDecimal.valueOf(position.getAmount()).multiply(BigDecimal.valueOf(position.getCurrentPrice())).setScale(2, BigDecimal.ROUND_HALF_UP).add(stockValue);
        }
        info.setStockValue(stockValue.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        info.setTotalValue(stockValue.add(BigDecimal.valueOf(user.getPrincipal())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        info.setProfit(info.getTotalValue() - info.getIntialPrincipal());
        double pct = BigDecimal.valueOf(info.getProfit() * 100).divide(BigDecimal.valueOf(info.getIntialPrincipal())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        info.setProfitPct(pct);
        return info;
    }


    public static StockPosition[] queryPosition(String username) {
        String sql = "select * from positions where amount > 0 and userid = '" + username + "' order by id desc";
        DbManager manager = InitServlet.getManager();
        List<StockPosition> list = new ArrayList<StockPosition>();
        BigDecimal totalStockValue = new BigDecimal(0);
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
                if(stockPrice.getPrice() <= 0) {
                    stockPrice.setPrice(stockPrice.getPreClose());
                }
                
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

                // calculate stockValue and stockValuePct
                // current*amount
                BigDecimal stockValue = BigDecimal.valueOf(stockPrice.getPrice()).multiply(BigDecimal.valueOf(amount));
                position.setStockValue(stockValue.doubleValue());

                totalStockValue = totalStockValue.add(stockValue);  
                logger.debug(position.getCurrentPrice() + ", " + position.getCostPrice() + ", " + position.getCode() + ", " + position.getProfit());
                list.add(position);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        StockPosition[] positions = new StockPosition[list.size()];
        for (int i = 0; i < list.size(); i++) {
            positions[i] = list.get(i);
            BigDecimal pct = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(positions[i].getStockValue()))
                    .divide(totalStockValue, 2, BigDecimal.ROUND_HALF_UP);
            positions[i].setStockValuePct(pct.doubleValue());
        }
        return positions;
    }


}
