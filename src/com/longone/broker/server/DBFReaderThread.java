package com.longone.broker.server;

import nl.knaw.dans.common.dbflib.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DBFReaderThread implements Runnable {
    private static final Logger logger = Logger.getLogger(DBFReaderThread.class);
    private static final String SH_FILE = "SHOW2003.DBF";
    private static final String SZ_FILE = "SJSHQ.DBF";

    // code, name, preClose, open, price, highest, lowest
    private static final String[] SH_FIELDS = {"S1", "S2", "S3", "S4", "S8", "S6", "S7"};
    private static final String[] SZ_FIELDS = {"HQZQDM", "HQZQJC", "HQZRSP", "HQJRKP",
            "HQZJCJ", "HQZGCJ", "HQZDCJ"};

    private static Map<String, StockPrice> data = new ConcurrentHashMap<String, StockPrice>();
    private boolean isStop = false;

    private DBFReaderThread() {
    }

    private static DBFReaderThread thread = new DBFReaderThread();

    public static DBFReaderThread getInstance() {
        return thread;
    }

    public void run() {
        logger.info("Start running DBFReaderThread....");
        while (!isStop) {
            loadFile(data, SH_FILE, SH_FIELDS);
            loadFile(data, SZ_FILE, SZ_FIELDS);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                logger.error(e);
            }
            logger.info("loading " + data.size() + " stocks from DBF....");
        }
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
            logger.error(e);
        } catch (CorruptedTableException e) {
            logger.error(e);
        } catch (DbfLibException e) {
            logger.error(e);
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
        //todo to be deleted....
        final double MAX_PRICE = 100.0; // $100.00
        Random random = new Random();

        stock.setPrice(Math.round(random.nextDouble() * MAX_PRICE * 100) / 100.0);
        //====end======
        stock.setHighest((Double) record.getNumberValue(fields[5]));
        stock.setLowest((Double) record.getNumberValue(fields[6]));
        return stock;
    }
}
