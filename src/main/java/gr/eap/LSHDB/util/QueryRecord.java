/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import gr.eap.LSHDB.Key;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dimkar
 */
public class QueryRecord extends Record implements Cloneable{
    
    public static final long serialVersionUID = 999L;
    
    int maxQueryRows;
    String storeName;
    
    boolean clientQuery = true;
    public boolean isClientQuery() {
        return clientQuery;
    }   
    public void setServerQuery() {
        clientQuery = false;
    }
    
    public static String QUERY_VALUE = "_Query";
    //String[] keyFieldNames;
    HashMap<String, QueryValueConf> conf = new HashMap<String, QueryValueConf>();
    ArrayList<String> queryFieldNames = new ArrayList<String>(); 
    
    public QueryValueConf getQueryValueConf(String fieldName){
           if (conf.containsKey(fieldName))
               return conf.get(fieldName);
        return null;   
    }
    
    public void set(String fieldName, Object fieldValue, double userPercentageThreshold, boolean performComparisons) {
        if (fieldValue != null) {
                set(fieldName, fieldValue);
                conf.put(fieldName, new QueryValueConf(userPercentageThreshold, performComparisons));
                queryFieldNames.add(fieldName);
        }
    }
    
    public QueryRecord setKeyedField(String fieldName, Object fieldValue, double userPercentageThreshold, boolean performComparisons){
        set(fieldName,"",userPercentageThreshold, performComparisons);
        set(fieldName+Key.TOKENS,fieldValue,userPercentageThreshold, performComparisons);        
        return this;
    }
    
    
    public void set(double userPercentageThreshold, boolean performComparisons) {
            conf.put("recordLevel", new QueryValueConf(userPercentageThreshold, performComparisons));
            queryFieldNames.add("recordLevel");
    }
    
    public ArrayList<String> getQueryFieldNames(){
        return this.queryFieldNames;
    }

    public boolean performComparisons(String fieldName) {
        if (conf.containsKey(fieldName)) {
            return conf.get(fieldName).performComparisons();
        }
        return true;
    }

    public double getUserPercentageThreshold(String fieldName) {
        if (conf.containsKey(fieldName)) {
            return conf.get(fieldName).getUserPercentageThreshold();
        } else {
            return 1.0;
        }
    }

    public QueryRecord(String storeName,int maxQueryRows) {
        super();    
        this.storeName = storeName;
        this.maxQueryRows = maxQueryRows;
    }
    
    public QueryRecord(String storeName,int maxQueryRows, Record rec) {
        this(storeName, maxQueryRows);    
        for (int i=0; i<rec.getFieldNames().size(); i++){
            String fieldName = rec.getFieldNames().get(i);            
            this.set(fieldName, rec.get(fieldName));
        }
    }
    

    public QueryRecord(int maxQueryRows) {
        super();    
        this.maxQueryRows = maxQueryRows;
    }
    
    public int getMaxQueryRows() {
        return maxQueryRows;
    }

    public String getStoreName() {
        return storeName;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    
   
}
