package com.longone.broker.servlet;

import com.longone.broker.server.DBFReaderThread;
import com.longone.broker.server.DbManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static Logger logger = null;
    public static final String DB_MANAGER = "dbManager";
    public static final String SYS_PROP = "sysProp";
    public static DBFReaderThread reader;

    private static final String JNDI_MOCKSTOCK = "jdbc/mockStock";
    private static final String LOG4J_CONFIG = "WEB-INF/log4j.properties";
    private static final String APP_CONFIG = "WEB-INF/config.properties";

    // Public constructor is required by servlet spec
    public ContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        initLog4j(context);
        logger = Logger.getLogger(ContextListener.class.getName());
        loadProperties(context);
        initDataPool(context);
        reader = new DBFReaderThread((Properties)context.getAttribute(SYS_PROP));
        new Thread(reader).start();
    }

    private void initDataPool(ServletContext context) {
        Properties prop = (Properties) context.getAttribute(SYS_PROP);
        DbManager manager = DbManager.getInstance(prop);
        context.setAttribute(DB_MANAGER, manager);
        logger.info("finish the init database pool....");
    }

    private void initLog4j(ServletContext context) {
        String webAppPath = context.getRealPath("/");
        String log4jProp = webAppPath + LOG4J_CONFIG;
        File file = new File(log4jProp);
        if (file.exists()) {
            PropertyConfigurator.configure(file.getPath());
            System.out.println("Log4j is initialized with: " + file.getPath());
        } else {
            BasicConfigurator.configure();
            System.err.println("*** " + log4jProp + " file not found, so initializing log4j with BasicConfigurator");
        }
    }

    private void loadProperties(ServletContext context) {
        String webAppPath = context.getRealPath("/");
        Properties sysProp = new Properties();
        try {
            sysProp.load(new FileInputStream(webAppPath + APP_CONFIG));
            context.setAttribute(SYS_PROP, sysProp);
            logger.info("application properties file loaded....");
            logger.debug(sysProp.get("SH_FILE"));
        } catch (IOException e) {
            logger.error("Failed to load application properties file", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info("===================contextDestroyed==============");
        reader.stopThread();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute
           is added to a session.
        */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}

