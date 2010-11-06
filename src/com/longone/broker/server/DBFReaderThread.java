package com.longone.broker.server;

import com.longone.broker.client.StockPrice;
import nl.knaw.dans.common.dbflib.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DBFReaderThread implements Runnable {
    private static final String SH_FILE = "SHOW2003.DBF";
    private static final String SZ_FILE = "SJSHQ.DBF";

    // code, name, preClose, open, price, highest, lowest
    private static final String[] SH_FIELDS = {"S1", "S2", "S3", "S4", "S8", "S6", "S7"};
    private static final String[] SZ_FIELDS = {"HQZQDM", "HQZQJC", "HQZRSP", "HQJRKP",
            "HQZJCJ", "HQZGCJ", "HQZDCJ"};

    private static Map<String, StockPrice> data = new HashMap<String, StockPrice>();
    private boolean isStop = false;

    private DBFReaderThread() {
    }

    private static DBFReaderThread thread = new DBFReaderThread();

    public static DBFReaderThread getInstance() {
        return thread;
    }

    public void run() {
        while (!isStop) {
            loadFile(data, SH_FILE, SH_FIELDS);
            loadFile(data, SZ_FILE, SZ_FIELDS);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        isStop = true;
    }

    public static Map<String, StockPrice> getData() {
        return data;
    }

    public static void main(String[] args) {

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
                data.put(code, createStockPrice(nameField, record, fields));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CorruptedTableException e) {
            e.printStackTrace();
        } catch (DbfLibException e) {
            e.printStackTrace();
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

    private static StockPrice createStockPrice(Field nameField, Record record, String[] fields) throws UnsupportedEncodingException, DbfLibException {
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
