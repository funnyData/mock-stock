package com.longone.broker.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Thread thread = new Thread(DBFReaderThread.getInstance());
        thread.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        DBFReaderThread.getInstance().stopThread();
    }
}
