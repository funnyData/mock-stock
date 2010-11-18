package com.longone.broker.server;

import nl.knaw.dans.common.dbflib.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DBFReaderThread implements Runnable {
    private static final Logger logger = Logger.getLogger(DBFReaderThread.class);

    // code, name, preClose, open, price, highest, lowest
    private static final String[] SH_FIELDS = {"S1", "S2", "S3", "S4", "S8", "S6", "S7"};
    private static final String[] SZ_FIELDS = {"HQZQDM", "HQZQJC", "HQZRSP", "HQJRKP",
            "HQZJCJ", "HQZGCJ", "HQZDCJ"};

    private static Map<String, StockPrice> data = new ConcurrentHashMap<String, StockPrice>();
    private boolean isStop = false;
    private String shFile;
    private String szFile;
    private DbManager manager;

    public DBFReaderThread(Properties properties, DbManager manager) {
        this.manager = manager;
        shFile = (String) properties.get("SH_FILE");
        szFile = (String) properties.get("SZ_FILE");
    }

    public void run() {
        logger.info("Start running DBFReaderThread....");
        int count = 0;
        while (!isStop) {
            loadFile(data, shFile, SH_FIELDS);
            loadFile(data, szFile, SZ_FIELDS);
            if (count % 60 == 0) {
                syncQuoteTable();
                count = 0;
            }
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                logger.error("DBFReader thread is interrupted during sleep", e);
            }
            count++;
        }
    }

    private void syncQuoteTable() {
        long start = new Date().getTime();
        logger.info("start synchronizing Quote Table.....");
        String sql = "select * from Quotes";
        try {
            // database -> map
            ResultSet set = manager.query(sql);
            Map<String, Double> dbMap = new HashMap<String, Double>();
            while (set.next()) {
                String code = set.getString("code");
                double preClose = set.getDouble("preClose");
                dbMap.put(code, preClose);
                if (data.get(code) == null) {
                    logger.info("Stock " + code + " not in DBF");
                    StockPrice stockPrice = new StockPrice();
                    stockPrice.setCode(code);
                    stockPrice.setName(set.getString("name"));
                    stockPrice.setPreClose(preClose);
                    stockPrice.setPrice(0);
                    data.put(code, stockPrice);
                }
            }

            // map -> database
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = fmt.format(new Date());
            Set<String> dbCode = dbMap.keySet();
            for (String code : data.keySet()) {
                if (dbCode.contains(code)) {
                    if (dbMap.get(code) != data.get(code).getPreClose()) {
                        StringBuffer buf = new StringBuffer("update Quotes set preClose = ");
                        buf.append(data.get(code).getPreClose()).append(", name='");
                        buf.append(data.get(code).getName()).append("', modified='");
                        buf.append(time).append("' where code = '");
                        buf.append(code).append("'");
                        manager.insertOrUpdate(buf.toString());
                    }
                } else {
                    logger.info("add new stock " + code);
                    StringBuffer buf = new StringBuffer("insert into Quotes (code, name, preClose, modified) values (");
                    buf.append("'").append(code).append("', ");
                    buf.append("'").append(data.get(code).getName()).append("', ");
                    buf.append(data.get(code).getPreClose()).append(", '");
                    buf.append(time).append("')");
                    manager.insertOrUpdate(buf.toString());
                }
            }
            logger.info("Synchronize Quote Table completed!!!");
            logger.info("elapse time: " + (new Date().getTime() - start) / 1000 + "s");
        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    private boolean isTodaySynced() {
        Calendar today = Calendar.getInstance();
        today.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
        today.add(Calendar.DATE, 1);
        String sql = "select max(modified) modified from quotes";
        ResultSet set = null;
        try {
            set = manager.query(sql);
            if (set.next()) {
                Calendar lastModified = Calendar.getInstance();
                lastModified.setTime(set.getDate("modified"));

            } else {
                return false;
            }
        }
        catch (SQLException e) {
            logger.error("", e);
        }
        return true;
    }


    public void stopThread() {
        logger.info("Stop running DBFReaderThread....");
        isStop = true;
    }

    public static Map<String, StockPrice> getData() {
        return data;
    }


    private static void loadFile(Map<String, StockPrice> data, String file, String[] fields) {
        final Table table = new Table(new File(file));
        String codeFieldStr = fields[0];
        String nameFieldStr = fields[1];
        try {
            table.open(IfNonExistent.ERROR);
            Field nameField = getNameField(table, nameFieldStr);
            final Iterator<Record> it = table.recordIterator();
            while (it.hasNext()) {
                final Record record = it.next();
                String code = record.getStringValue(codeFieldStr);
                data.put(code, populateStockPrice(nameField, record, fields));
            }
        } catch (IOException e) {
            logger.error("", e);
        } catch (CorruptedTableException e) {
            logger.error("", e);
        } catch (DbfLibException e) {
            logger.error("", e);
        }
    }

    private static Field getNameField(Table table, String fieldName) {
        List<Field> fields = table.getFields();
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private static StockPrice populateStockPrice(Field nameField, Record record, String[] fields) throws UnsupportedEncodingException, DbfLibException {
        StockPrice stock = new StockPrice();
        stock.setCode(record.getStringValue(fields[0]));
        stock.setName(new String(record.getRawValue(nameField), "GBK"));
        stock.setPreClose((Double) record.getNumberValue(fields[2]));
        stock.setOpen((Double) record.getNumberValue(fields[3]));
        stock.setPrice((Double) record.getNumberValue(fields[4]));
        stock.setHighest((Double) record.getNumberValue(fields[5]));
        stock.setLowest((Double) record.getNumberValue(fields[6]));
        return stock;
    }
}
