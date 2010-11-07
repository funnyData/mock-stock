package com.longone.broker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.longone.broker.client.DealLog;
import com.longone.broker.client.StockPosition;
import com.longone.broker.client.StockService;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockServiceImpl extends RemoteServiceServlet implements StockService {
    private static final Logger logger = Logger.getLogger(StockServiceImpl.class);
    private static double COMMISSION_PERCENTAGE = 0.003;
    private static String SYS_ERROR = "系统出错，请联系管理员";

    public StockPosition[] getStockPositions() {
        String user = getCurrentUser();

        return queryPosition(user);
    }

    public String placeOrder(String code, int amount, boolean isBuy) {
        String user = getCurrentUser();

        logger.info("place order (" + code + ", " + amount + "," + isBuy + ")");
        // check if stock code is correct
        StockPrice stockPrice = DBFReaderThread.getData().get(code);
        if (stockPrice == null) {
            logger.info(code + " not existed...");
            return "股票代码(" + code + ")不存在！！！";
        }

        // check if amount or principal is enough
        double currentPrice = stockPrice.getPrice();
        double buyExpense = currentPrice * amount * (1 + COMMISSION_PERCENTAGE);
        double principal = queryPrincipal(user);
        StockPosition position = queryPositionByCode(code, user);
        position.setName(stockPrice.getName());
        if (isBuy && buyExpense > principal) {
            logger.info("currentPrice: " + currentPrice + "amount:" + amount + " principal:" + principal);
            logger.info("principal is not enough for this order");
            return "金额不足！！！";
        } else if (!isBuy && amount > position.getAmount()) {
            logger.info("position amount: " + position.getAmount() + "amount:" + amount + " principal:" + principal);
            logger.info("position amount is not enough for this order");
            return "股票数量不足！！！";
        }

        // start deal
        return insertDeal(user, position, currentPrice, amount, isBuy, principal);
    }

    private String getCurrentUser() {
        return "wfei";
    }

    public String resetPassword(String password) {
        String user = getCurrentUser();
        String sql = "update users set password = '" + password + "' where username='" + user + "'";
        DbManager manager = InitServlet.getManager();
        String error;
        try {
            int count = manager.insertOrUpdate(sql);
            if (count != 1) {
                logger.error("failed to update password for " + user);
                logger.error("reset password is " + password);
                return SYS_ERROR;
            }
        } catch (SQLException e) {
            logger.error(e);
            return SYS_ERROR;
        }
        return "密码更新成功！！！";
    }

    public DealLog[] getDealLogs() {
        String sql = "select * from dealLogs where userid = '" + getCurrentUser() + "' order by id desc";
        DbManager manager = InitServlet.getManager();
        List<DealLog> list = new ArrayList<DealLog>();
        try {
            ResultSet set = manager.query(sql);
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (set.next()) {
                DealLog deal = new DealLog();
                deal.setCode(set.getString("code"));
                deal.setName(set.getString("name"));
                deal.setBs(set.getString("bs"));
                deal.setPrice(set.getDouble("price"));
                deal.setAmount(set.getInt("amount"));
                deal.setCommission(set.getDouble("commission"));
                deal.setCreated(fmt.format(set.getDate("created")));
                list.add(deal);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        DealLog[] logs = new DealLog[list.size()];
        for (int i = 0; i < list.size(); i++) {
            logs[i] = list.get(i);
        }
        return logs;
    }

    private String insertDeal(String user, StockPosition position, double currentPrice, int amount,
                              boolean buy, double principal) {

        double commission = currentPrice * amount * COMMISSION_PERCENTAGE;
        // generate dealLogs sql
        StringBuffer dealLogsSql = new StringBuffer("insert into dealLogs (code, name, bs, price, amount, commission, userid, created) values ('");
        dealLogsSql.append(position.getCode()).append("', '");
        dealLogsSql.append(position.getName()).append("', '");
        String bs;
        if (buy) {
            bs = "买入";
        } else {
            bs = "卖出";
        }
        dealLogsSql.append(bs).append("', ");
        dealLogsSql.append(currentPrice).append(", ");
        dealLogsSql.append(amount).append(", ");
        dealLogsSql.append(commission).append(", '");
        dealLogsSql.append(user).append("', '");
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dealLogsSql.append(fmt.format(new Date())).append("')");

        double expense;
        double newPrincipal;
        // generate users sql
        if (buy) {
            expense = (currentPrice * amount) + commission;
            newPrincipal = principal - expense;
        } else {
            expense = (currentPrice * amount) - commission;
            newPrincipal = principal + expense;
        }
        StringBuffer usersSql = new StringBuffer("update users set principal = ");
        usersSql.append(newPrincipal).append("where username = '").append(user).append("'");

        // generate positions sql
        StringBuffer positionsSql = new StringBuffer();

        if (position.getAmount() > 0) {
            double newCommission = position.getCommission() + commission;
            int newAmount;
            double newCost;
            double realProfit = 0;
            double lastAmount = 0;
            if (buy) {
                newAmount = position.getAmount() + amount;
                //newCost = (position.getCostPrice() * position.getAmount() + expense) / newAmount;
                newCost = BigDecimal.valueOf(position.getCostPrice()).multiply(BigDecimal.valueOf(position.getAmount()))
                        .add(BigDecimal.valueOf(expense)).divide(BigDecimal.valueOf(newAmount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            } else {
                newAmount = position.getAmount() - amount;
                //newCost = (position.getCostPrice() * position.getAmount() - expense) / newAmount;
                if (newAmount == 0) {
                    newCost = position.getCostPrice();
                    lastAmount = position.getAmount();
                    realProfit = expense - (position.getCostPrice() * position.getAmount());
                } else {
                    newCost = BigDecimal.valueOf(position.getCostPrice()).multiply(BigDecimal.valueOf(position.getAmount()))
                            .subtract(BigDecimal.valueOf(expense)).divide(BigDecimal.valueOf(newAmount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
            }
            positionsSql.append("update positions set amount = ");
            positionsSql.append(newAmount).append(", cost=");
            positionsSql.append(newCost).append(", commission=");
            positionsSql.append(newCommission).append(", realProfit=");
            positionsSql.append(realProfit).append(", lastAmount=");
            positionsSql.append(lastAmount).append(" where code='");
            positionsSql.append(position.getCode()).append("' and userid='");
            positionsSql.append(user).append("'");
        } else {
            // cost = expense / newAmount;
            double cost = BigDecimal.valueOf(expense).divide(BigDecimal.valueOf(amount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

            positionsSql.append("insert into positions(code, name, amount, cost, commission, userid) values('");
            positionsSql.append(position.getCode()).append("', '");
            positionsSql.append(position.getName()).append("', ");
            positionsSql.append(amount).append(", ");
            positionsSql.append(cost).append(", ");
            positionsSql.append(commission).append(", '");
            positionsSql.append(user).append("')");
        }


        DbManager manager = InitServlet.getManager();
        try {
            int count = manager.insertOrUpdate(dealLogsSql.toString());
            if (count != 1) {
                logger.error("update/insert deallogs table failed....");
                return SYS_ERROR;
            }
            count = manager.insertOrUpdate(usersSql.toString());
            if (count != 1) {
                logger.error("update/insert users table failed....");
                return SYS_ERROR;
            }
            count = manager.insertOrUpdate(positionsSql.toString());
            if (count != 1) {
                logger.error("update/insert positions table failed....");
                return SYS_ERROR;
            }
        } catch (SQLException e) {
            logger.error(e);
            return SYS_ERROR;
        }
        DecimalFormat decimalfmt = new DecimalFormat("#,##0.00");
        return "下单成功，" + bs + amount +
                "股\"" + position.getName() + "\", 交易价格:" + currentPrice + ",产生佣金:" + decimalfmt.format(commission);
    }


    private StockPosition queryPositionByCode(String code, String user) {
        StockPosition position = new StockPosition();
        position.setCode(code);
        position.setAmount(0);

        String sql = "select * from positions where userid = '" + user + "' and code = '" + code + "'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            if (set.next()) {
                position.setCostPrice(set.getDouble("cost"));
                position.setCommission(set.getDouble("commission"));
                position.setAmount(set.getInt("amount"));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return position;
    }

    private double queryPrincipal(String user) {
        String sql = "select principal from users where username = '" + user + "'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            if (set.next()) {
                return set.getDouble("principal");
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.error(user + "not found....");
        return 0;
    }

    private StockPosition[] queryPosition(String user) {
        String sql = "select * from positions where amount > 0 and userid = '" + user + "' order by id desc";
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
                BigDecimal profit = BigDecimal.valueOf(stockPrice.getPrice()).subtract(BigDecimal.valueOf(cost)).multiply(BigDecimal.valueOf(amount));
                position.setProfit(profit.doubleValue());

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


    public static void main(String[] args) {
        //64.04, 8.39, 600710, 834750.0000000001
        double price = 64.04;
        double cost = 8.39;
        int amount = 15000;
        BigDecimal profit = BigDecimal.valueOf(price).subtract(BigDecimal.valueOf(cost)).multiply(BigDecimal.valueOf(amount));

        System.out.println(profit.doubleValue());
    }
}
