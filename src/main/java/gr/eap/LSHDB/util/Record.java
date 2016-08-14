/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import gr.eap.LSHDB.Key;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author dimkar
 */
public class Record implements Serializable {
    @JsonIgnore
    public static final long serialVersionUID = 1000L;
    
    @JsonIgnore
    public HashMap<String, Object> record = new HashMap<String, Object>();

    @JsonIgnore
    public HashMap<String, Integer> notIndexedFields = new HashMap<String, Integer>();

    @JsonIgnore
    public static String PRIVATE_STRUCTURE = "PRIVATE";
    @JsonIgnore
    public static String REMOTE_RECORD = "remote";


    @JsonValue
    public HashMap toJsonObject() {
        Iterator<Map.Entry<String, Object>> iter = record.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            if ((entry.getKey().endsWith(Key.TOKENS))) {
                iter.remove();
            }
        }
        record.put(REMOTE_RECORD,remote);
        return record;
    }

    @JsonIgnore
    boolean remote = false;

        
    @JsonIgnore
    public boolean isRemote() {
        return remote;
    }

    @JsonIgnore
    public void setRemote() {
        remote = true;
    }

    @JsonIgnore
    public void set(String fieldName, Object fieldValue) {
        record.put(fieldName, fieldValue);
    }

    @JsonIgnore
    public void setNotIndexedField(String fieldName) {
        notIndexedFields.put(fieldName, 1);
    }

    @JsonIgnore
    public boolean isNotIndexedField(String fieldName) {
        return notIndexedFields.containsKey(fieldName);
    }

    @JsonIgnore
    public Object get(String fieldName) {
        return record.get(fieldName);
    }

    @JsonIgnore
    public String getIdFieldName() {
        return "Id";
    }

    @JsonIgnore
    public String getId() {
        return (String) record.get("Id");
    }

    @JsonIgnore
    public void setId(String id) {
        record.put("Id", id);
    }

    @JsonIgnore
    public ArrayList<String> getFieldNames() {
        ArrayList<String> arr = new ArrayList<String>();
        Iterator it = record.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String fieldName = (String) pair.getKey();
            if (!fieldName.equals("Id") && (!fieldName.endsWith(Key.TOKENS))) {
                arr.add(fieldName);
            }
        }
        return arr;
    }
}
