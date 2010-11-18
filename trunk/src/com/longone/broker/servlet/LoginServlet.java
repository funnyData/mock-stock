package com.longone.broker.servlet;

import com.longone.broker.client.User;
import com.longone.broker.server.DbManager;
import com.longone.broker.server.ProfitCalculator;
import com.longone.broker.server.StockServiceImpl;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(LoginServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sql = "select * from users where username='dhzq'";
        DbManager manager = InitServlet.getManager();
        try {
            ResultSet set = manager.query(sql);
            set.next();
            User user = ProfitCalculator.createUser(set);
            HttpSession session = request.getSession();
            session.setAttribute(StockServiceImpl.USER, user);
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.info("someone login as dhzq from OA.....");
        response.sendRedirect(request.getContextPath() + "/MockStock.html?mockstock=1");
    }
}
