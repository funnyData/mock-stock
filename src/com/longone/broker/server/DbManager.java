package com.longone.broker.server;

import com.sun.rowset.CachedRowSetImpl;
import org.apache.log4j.Logger;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.Properties;

public final class DbManager {
    private static Logger logger = Logger.getLogger(DbManager.class);
    private static DbManager manager = null;
    private static String url = null;
    private static String userName = null;
    private static String password = null;

    public static DbManager getInstance(Properties prop) {
        if (manager == null) {
            return new DbManager(prop);
        } else {
            return manager;
        }
    }

    private DbManager(Properties prop) {
        url = (String) prop.get("dbUrl");
        userName = (String) prop.get("dbUser");
        password = (String) prop.get("dbPwd");
        try {
            Class.forName((String) prop.get("dbDriver"));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }


    public ResultSet query(String sql) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        return executeQuery(conn, stmt, sql, null);
    }

    public int insertOrUpdate(String sql) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        return executeUpdateInsert(conn, stmt, sql, null);
    }

    private ResultSet executeQuery(Connection conn, PreparedStatement stmt, String sql, String[] strings) throws SQLException {
        return executeQuery(conn, stmt, sql, strings, true);
    }

    private ResultSet executeQuery(Connection conn, PreparedStatement stmt, String sql, String[] strings, boolean isDebug) throws SQLException {

        if (isDebug && logger.isDebugEnabled()) {
            logger.debug("Execute below SQL statment: ");
            logger.debug(getStatementStr(sql, strings));
        }
        ResultSet set = stmt.executeQuery();

        CachedRowSet cached = new CachedRowSetImpl();
        cached.populate(set);

        set.close();
        stmt.close();
        conn.close();
        return cached;
    }

    private Object getStatementStr(String sql, String[] params) {
        if (params == null) {
            return sql;
        }
        for (String param : params) {
            sql = sql.replaceFirst("\\?", param == null ? "null" : param);
        }
        return sql;
    }

    private int executeUpdateInsert(Connection conn, PreparedStatement stmt, String sql, String[] strings) throws SQLException {
        return executeUpdateInsert(conn, stmt, sql, strings, true);
    }

    private int executeUpdateInsert(Connection conn, PreparedStatement stmt, String sql, String[] strings, boolean isDebug) throws SQLException {
        if (isDebug && logger.isDebugEnabled()) {
            logger.debug("Execute below SQL statment: ");
            logger.debug(getStatementStr(sql, strings));
        }
        int count = stmt.executeUpdate();

        stmt.close();
        conn.close();
        return count;
    }
}
