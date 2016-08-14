/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import gr.eap.LSHDB.DataStore;
import gr.eap.LSHDB.Key;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author dimkar
 */
public class Result implements Serializable {
    
    public static final long serialVersionUID = 555L; 
    
    public static int STATUS_OK = 0;
    public static int STORE_NOT_FOUND = 1;
    public static int NO_QUERY_VALUES_SPECIFIED = 2;
    public static int NO_KEYED_FIELDS_SPECIFIED = 3;

    public static String STORE_NOT_FOUND_ERROR_MSG = "The specified store does not exist.";
    public static String NO_QUERY_VALUES_SPECIFIED_ERROR_MSG = "No query values specified.";
    public static String NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG = "No (valid) keyed fields (and values) specified.";
    

    public QueryRecord queryRecord;

    int status = 0;

    transient HashMap<String, RecordList> recordListMap = new HashMap<String, RecordList>();

    String msg = "";
    double time; // seconds

    transient HashMap<String, Record> globalMap;

    ArrayList<Record> resultList = new ArrayList<Record>();

    
    
    
    
    public HashMap<String, Record> getMap(String fieldName) {
        if (recordListMap.containsKey(fieldName)) {
            return recordListMap.get(fieldName).m;
        }
        return null;
    }

    public void setStatus(int s) {
        this.status = s;
    }

    public int getStatus() {
        return this.status;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<Record> getRecords() {  // should be called after th server-side prepare()
        return this.resultList;
    }

    public ArrayList<Record> getUniqueRecords(String fieldName) {  // should be called after th server-side prepare()
        if (queryRecord.get(fieldName) == null) {
            return null;
        }
        HashMap map = new HashMap();
        for (int i = 0; i < this.resultList.size(); i++) {
            Record rec = this.resultList.get(i);
            Object v = rec.get(fieldName);            
            map.put(v, fieldName);
        }
        Set<String> keys = map.keySet(); // The set of keys in the map.
        Iterator<String> keyIter = keys.iterator();
        ArrayList<Record> arr=new ArrayList<Record>();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            Record rec=new Record();
            rec.set(fieldName, key);
            arr.add(rec);
        }
        return arr;
    }

    public void prepare() {
        ArrayList<String> fieldNames = queryRecord.getQueryFieldNames(); //   queryRecord.getFieldNames();  // 
        HashMap<String, HashMap<String, Record>> mapField = new HashMap<String, HashMap<String, Record>>();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            if (fieldName.endsWith(Key.TOKENS)) {
                continue;             
            }
            HashMap<String, Record> map = recordListMap.get(fieldName).getRecords();
            mapField.put(fieldName, map);
            if (i == 0) {
                globalMap = new HashMap<String, Record>(map);
            } else {
                globalMap.keySet().retainAll(map.keySet());
                // try with entrySet
            }
        }

        Iterator it = globalMap.keySet().iterator();
        while (it.hasNext()) {
            String id = (String) it.next();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                HashMap<String, Record> map = mapField.get(fieldName);
                if (map.containsKey(id)) {
                    Record rec = recordListMap.get(fieldName).getRecords().get(id);
                    resultList.add(rec);
                    break;
                }
            }
            it.remove();
        }
    }

    public Result(QueryRecord rec) {
        queryRecord = rec;
        ArrayList<String> fieldNames = queryRecord.getQueryFieldNames();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            QueryValueConf conf = queryRecord.getQueryValueConf(fieldName);
            recordListMap.put(fieldName, new RecordList(fieldName, queryRecord.getMaxQueryRows(), conf.performComparisons()));
        }
    }

    public boolean add(String fieldName, Record rec) {       
        if (recordListMap.containsKey(fieldName)) {            
            return recordListMap.get(fieldName).add(rec);
        }
        return false;
    }

    public Record getQueryRecord() {
        return queryRecord;
    }

   
    public int getDataRecordsSize(String fieldName) {
        if (recordListMap.containsKey(fieldName)) {
            return recordListMap.get(fieldName).getDataRecordsSize();
        }
        return 0;
    }

  
    
}
