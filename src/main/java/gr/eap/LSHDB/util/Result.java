/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import gr.eap.LSHDB.JSON;
import gr.eap.LSHDB.Key;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author dimkar
 */
public class Result implements Serializable {

    public static final long serialVersionUID = 555L;

    public final static int STATUS_OK = 0;
    public final static int STORE_NOT_FOUND = 1;
    public final static int NO_QUERY_VALUES_SPECIFIED = 2;
    public final static int NO_KEYED_FIELDS_SPECIFIED = 3;
    public final static int NULL_RESULT_RETURNED = 4;
     public final static int NO_CONNECT = 5;

    public final static String STORE_NOT_FOUND_ERROR_MSG = "The specified store does not exist.";
    public final static String NO_QUERY_VALUES_SPECIFIED_ERROR_MSG = "No query values specified.";
    public final static String NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG = "No (valid) keyed fields (and values) specified.";

    public QueryRecord queryRecord;

    int status = 0;

    transient HashMap<String, RecordList> recordListMap = new HashMap<String, RecordList>();

    String msg = "";
    double time; // seconds

    transient HashMap<String, Record> globalMap;

    AtomicInteger pairsNo = new AtomicInteger(0);

    public void incPairsNo() {
        pairsNo.set(pairsNo.incrementAndGet());
    }

    public void incPairsNoBy(int x) {
        pairsNo.set(pairsNo.get()+x);
    }
    
    public int getPairsNo() {
        return pairsNo.get();
    }

    HashMap<String, Integer> distributedStatusMap=new HashMap<String, Integer>();
    public void setStatus(String server, int error){
        distributedStatusMap.put(server,error);
    }
    public Iterator getStatusIterator(){
         Iterator it = distributedStatusMap.entrySet().iterator();
         return it;
    }
    public HashMap<String, Integer> getStatusMap(){
        return distributedStatusMap;
    }
    
    ArrayList<Record> resultList = new ArrayList<Record>();

    boolean remote = false;

    public boolean isRemote() {
        return remote;
    }

    public void setRemote() {
        remote = true;
        if (this.getRecords() != null) {
            for (int i = 0; i < this.getRecords().size(); i++) {
                this.getRecords().get(i).setRemote();
            }
        }
    }

    String origin;

    public void setOrigin(String server) {
        origin = server;
    }

    public String getOrigin() {
        return origin;
    }

    public synchronized HashMap<String, Record> getMap(String fieldName) {
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
        ArrayList<Record> arr = new ArrayList<Record>();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            Record rec = new Record();
            rec.set(fieldName, key);
            arr.add(rec);
        }
        return arr;
    }

    public void prepare() {       
        ArrayList<String> fieldNames = queryRecord.getQueryFieldNames(); //   queryRecord.getFieldNames();  // 
        HashMap<String, LinkedHashMap<String, Record>> mapField = new HashMap<String, LinkedHashMap<String, Record>>();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            if (fieldName.endsWith(Key.TOKENS)) {
                continue;
            }
            LinkedHashMap<String, Record> map = recordListMap.get(fieldName).getRecords();
            mapField.put(fieldName, map);
            if (i == 0) {
                globalMap = new LinkedHashMap<String, Record>(map);
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

    public String asJSON(){
        JSON j = new JSON();
        return j.prepare(this);
    }
    
    public ArrayList<Record> asList(){
        prepare();
        return this.getRecords();
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

    
    
    
    public synchronized boolean add(String fieldName, Record rec) {
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
