package com.longone.broker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.longone.broker.client.*;
import com.longone.broker.servlet.InitServlet;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class StockServiceImpl extends RemoteServiceServlet implements StockService {
    private static final Logger logger = Logger.getLogger(StockServiceImpl.class);
    private static final double COMMISSION_PERCENTAGE = 0.003;
    private static final String SYS_ERROR = "系统出错，请联系管理员";
    public static final String USER = "USER_SESSION_ATTR";

    public StockPosition[] getStockPositions() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return ProfitCalculator.queryPosition(user.getUsername());
    }

    public StockPosition[] getStockPositions(String username) {
        return ProfitCalculator.queryPosition(username);
    }


    public String placeOrder(String code, int amount, boolean isBuy) {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }

        logger.info("place order (" + code + ", " + amount + "," + isBuy + ")");
        // check if stock code is correct
        StockPrice stockPrice = DBFReaderThread.getData().get(code);
        if (stockPrice == null) {
            logger.info(code + " not existed...");
            return "股票代码(" + code + ")不存在！！！";
        } else if (stockPrice.getPrice() == 0) {
            logger.info(code + " price is zero....");
            return "股票(" + code + ")目前不能交易！！！";
        }
        return doDeal(code, stockPrice.getName(), stockPrice.getPrice(), amount, isBuy, user.getUsername());
    }

    private String doDeal(String code, String stockName, double currentPrice, int amount, boolean isBuy, String username) {
        // check if amount or principal is enough
        double buyExpense = BigDecimal.valueOf(currentPrice).multiply(BigDecimal.valueOf(amount))
                .multiply(BigDecimal.valueOf(1 + COMMISSION_PERCENTAGE)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double principal = queryPrincipal(username);
        StockPosition position = queryPositionByCode(code, username);
        position.setName(stockName);

        logger.debug("Try buy/sell stock: " + code);
        logger.debug("principal: " + principal);
        logger.debug("buyExpense: " + buyExpense);
        logger.debug("currentPrice: " + currentPrice + ", amount:" + amount);
        logger.debug("=================================================");
        if (isBuy && buyExpense > principal) {
            logger.info("currentPrice: " + currentPrice + "amount:" + amount + " principal:" + principal);
            logger.info("principal is not enough for this order");
            return "金额不足！！！(可用资金:￥" + principal + ")";
        } else if (!isBuy && amount > position.getAmount()) {
            logger.info("position amount: " + position.getAmount() + "amount:" + amount + " principal:" + principal);
            logger.info("position amount is not enough for this order");
            return "股票数量不足！！！(可卖出股数:" + amount + ")";
        }

        // start deal
        return insertDeal(username, position, currentPrice, amount, isBuy, principal);
    }

    private User getCurrentUser() {
        HttpSession session = this.getThreadLocalRequest().getSession();
        User user = (User) session.getAttribute(USER);
        if(user == null){
            return user;
        }
        String sql = "select principal from users where username='" + user.getUsername() + "'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            while (set.next()) {
                user.setPrincipal(set.getDouble("principal"));
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return user;
    }

    public String resetPassword(String password) {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        String sql = "update users set password = '" + password + "' where username='" + user.getUsername() + "'";
        DbManager manager = InitServlet.getManager();
        try {
            int count = manager.insertOrUpdate(sql);
            if (count != 1) {
                logger.error("failed to update password for " + user.getUsername());
                logger.error("reset password is " + password);
                return SYS_ERROR;
            }
        } catch (SQLException e) {
            logger.error("", e);
            return SYS_ERROR;
        }
        return "密码更新成功！！！";
    }

    public DealLog[] getDealLogs(String username) {
        String sql = "select * from dealLogs where userid = '" + username + "' order by id desc";
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
            logger.error("", e);
        }
        DealLog[] logs = new DealLog[list.size()];
        for (int i = 0; i < list.size(); i++) {
            logs[i] = list.get(i);
        }
        return logs;
    }


    public DealLog[] getDealLogs() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return getDealLogs(user.getUsername());
    }

    public User login(String username, String password) {
        String sql = "select * from users where enabled = 'Y' and username = '" + username + "'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            if (set.next()) {
                if (password.equals(set.getString("password"))) {
                    User user = ProfitCalculator.createUser(set);
                    HttpSession session = this.getThreadLocalRequest().getSession();
                    session.setAttribute(USER, user);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return null;
    }


    public AccountInfo getAccountInfo() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return ProfitCalculator.getAccountInfo(user);
    }

    public AccountInfo[] getAllAccountInfo() {
        User user = getCurrentUser();
        if (user == null || !"Y".equals(user.getSuperUser())) {
            return null;
        }
        return ProfitCalculator.getAllAccountInfo();
    }


    private String insertDeal(String username, StockPosition position, double currentPrice, int amount,
                              boolean buy, double principal) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = fmt.format(new Date());
        // commission = currentPrice * amount * COMMISSION_PERCENTAGE
        double commission = BigDecimal.valueOf(currentPrice).multiply(BigDecimal.valueOf(amount))
                .multiply(BigDecimal.valueOf(COMMISSION_PERCENTAGE)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        logger.debug("commission for this deal: " + commission);
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
        dealLogsSql.append(username).append("', '");
        dealLogsSql.append(now).append("')");

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
        usersSql.append(newPrincipal).append(" where username = '").append(username).append("'");

        // generate positions sql
        StringBuffer positionsSql = new StringBuffer();


        if (position.getAmount() > 0) {
            double newCommission = position.getCommission() + commission;
            int newAmount;
            double newCost;
            double profit;
            if (buy) {
                newAmount = position.getAmount() + amount;
                //newCost = (-1*position.getProfit() + expense) / newAmount;
                newCost = BigDecimal.valueOf(expense).subtract(BigDecimal.valueOf(position.getProfit()))
                        .divide(BigDecimal.valueOf(newAmount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                profit = position.getProfit() - expense;
            } else {
                newAmount = position.getAmount() - amount;

                if (newAmount == 0) {
                    newCost = position.getCostPrice();
                } else {
                    //newCost = -1*(expense + position.getProfit()) / newAmount;
                    newCost = BigDecimal.valueOf(expense).add(BigDecimal.valueOf(position.getProfit()))
                            .divide(BigDecimal.valueOf(newAmount), 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(-1)).doubleValue();
                }
                profit = position.getProfit() + expense;
            }
            logger.debug("expense: " + expense);
            positionsSql.append("update positions set amount = ");
            positionsSql.append(newAmount).append(", cost=");
            positionsSql.append(newCost).append(", commission=");
            positionsSql.append(newCommission).append(", profit=");
            positionsSql.append(profit).append(", modified='");
            positionsSql.append(now).append("' where amount > 0 and code='");
            positionsSql.append(position.getCode()).append("' and userid='");
            positionsSql.append(username).append("'");
        } else {
            // cost = expense / newAmount;
            double cost = BigDecimal.valueOf(expense).divide(BigDecimal.valueOf(amount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

            positionsSql.append("insert into positions(code, name, amount, cost, commission, profit,");
            positionsSql.append("userid, created, modified) values('");
            positionsSql.append(position.getCode()).append("', '");
            positionsSql.append(position.getName()).append("', ");
            positionsSql.append(amount).append(", ");
            positionsSql.append(cost).append(", ");
            positionsSql.append(commission).append(", ");
            positionsSql.append(-expense).append(", '");
            positionsSql.append(username).append("', '");
            positionsSql.append(now).append("', '");
            positionsSql.append(now).append("')");
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
            logger.error("", e);
            return SYS_ERROR;
        }
        DecimalFormat decimalfmt = new DecimalFormat("#,##0.00");
        return "下单成功，" + bs + amount +
                "股\"" + position.getName() + "\", 交易价格:" + currentPrice + ",产生佣金:" + decimalfmt.format(commission);
    }


    private StockPosition queryPositionByCode(String code, String username) {
        StockPosition position = new StockPosition();
        position.setCode(code);
        position.setAmount(0);

        String sql = "select * from positions where userid = '" + username + "' and code = '" + code + "' order by id desc";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            if (set.next()) {
                position.setCostPrice(set.getDouble("cost"));
                position.setCommission(set.getDouble("commission"));
                position.setAmount(set.getInt("amount"));
                position.setProfit(set.getDouble("profit"));
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return position;
    }

    private double queryPrincipal(String username) {
        String sql = "select principal from users where username = '" + username + "'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            if (set.next()) {
                return set.getDouble("principal");
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        logger.error(username + "not found....");
        return 0;
    }


    public static void main(String[] args) {

        Properties prop = new Properties();
        prop.put("dbUrl", "jdbc:sqlserver://172.10.1.143:1433;databaseName=MockStock");
        prop.put("dbUser", "sa");
        prop.put("dbPwd", "sa");

        String[][] data0 = {
                {"600178", "东安动力", "10000", "11.92", "true"},
                {"600439", "瑞贝卡", "20000", "10.93", "true"},
                {"000968", "煤气化", "10000", "20.68", "true"},
                {"000965", "天保基建", "15000", "13.32", "true"},
                {"000407", "胜利股份", "10000", "7.68", "true"},
                {"601877", "正泰电器", "3000", "22.94", "true"},
                {"600439", "瑞贝卡  ", "5000", "11.7", "true"},
                {"600439", "瑞贝卡  ", "5000", "12.31", "false"},
                {"000938", "紫光股份", "5000", "15.69", "true"},
                {"600439", "瑞贝卡  ", "5000", "14.1", "false"},
                {"600439", "瑞贝卡  ", "5000", "13.5", "true"},
                {"000407", "胜利股份", "10000", "8.06", "false"},
                {"000938", "紫光股份", "5000", "15.72", "true"}
        };
        execute(prop, data0, "test");
    }

    private static void execute(Properties prop, String[][] data, String username) {

        System.out.println(username);
        System.out.println("-----------------");
        StockServiceImpl service = new StockServiceImpl();
        DbManager manager = DbManager.getInstance(prop);
        InitServlet.setManager(manager);
        for (String[] record : data) {
            String result = service.doDeal(record[0], record[1], Double.parseDouble(record[3]), Integer.parseInt(record[2]), Boolean.parseBoolean(record[4]), username);
            System.out.println(result);
        }
        System.out.println("-----------------");
        System.out.println();
    }
}
