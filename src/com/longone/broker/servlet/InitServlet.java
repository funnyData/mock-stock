package com.longone.broker.servlet;

import com.longone.broker.server.DBFReaderThread;
import com.longone.broker.server.DbManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Properties;

public class InitServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(InitServlet.class);
    private static DbManager manager = null;
    private static Properties prop = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        manager = (DbManager) context.getAttribute(ContextListener.DB_MANAGER);
        prop = (Properties) context.getAttribute(ContextListener.SYS_PROP);
        super.init(config);
    }

    public static DbManager getManager() {
        return manager;
    }

    public static Properties getProp() {
        return prop;
    }

    public static void setManager(DbManager manager) {
        InitServlet.manager = manager;
    }
}
