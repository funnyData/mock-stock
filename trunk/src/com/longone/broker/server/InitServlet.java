package com.longone.broker.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
    private static DbManager manager = null;

    @Override
    public void init(ServletConfig config) throws ServletException {

        ServletContext context = config.getServletContext();
        manager = (DbManager) context.getAttribute(ContextListener.DB_MANAGER);
        super.init(config);
    }

    @Override
    public void destroy() {
        super.destroy();
        DBFReaderThread.getInstance().stopThread();
    }

    public static DbManager getManager() {
        return manager;
    }
}
