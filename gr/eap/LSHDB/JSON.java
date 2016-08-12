/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.URLUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author dimkar
 */
public class JSON {
    
    public static String JSON_REQUEST = "/JSON/";

     public String toJSON(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        
        //Object to JSON in String
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(o);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonInString;
    }

    public Map<String, Object> fromJSON(String s) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = mapper.readValue(s, Map.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }
    
    public String getDbName(String msg){
        msg = msg.replaceAll(JSON_REQUEST, "");
        String dbName = msg.substring(0, msg.indexOf("?"));
        return dbName;
    }
    
    
    public String convert(String msg, int queryNo, DataStore db) {
        //System.out.println("JSON= " + msg);
        String s = "";
        msg = msg.replaceAll(JSON_REQUEST, "");
        String dbName = msg.substring(0, msg.indexOf("?"));
        msg = msg.replaceAll(dbName, "");
        msg = msg.replaceAll("\\?", "");
        msg = msg.substring(0, msg.indexOf(' '));
        QueryRecord query = new QueryRecord(dbName, queryNo);
        String callback = URLUtil.getRequestKey(msg, "callback");
        String returnField = URLUtil.getRequestKey(msg, "returnField");

        Map<String, String> m = URLUtil.splitQuery(msg);

        if (db != null) {
            Iterator it = m.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                query.set(pair.getKey() + "", pair.getValue());

                if (db.getConfiguration().isKeyed) {
                    String value = (String) pair.getValue();
                    //System.out.println("name="+pair.getKey()+" value=" + value + ".");
                    String[] values = value.split(" ");
                    query.set(pair.getKey() + "", value, 1.0, true);
                    query.set(pair.getKey() + "_tokens", values, 1.0, true);
                } else {
                    query.set(pair.getKey() + "", pair.getValue());
                }

                //System.out.println(pair.getKey() + " = " + pair.getValue());
            }

            if (!db.getConfiguration().isKeyed) {
                query.set(1.0, true);
            }

            long tStartInd = System.nanoTime();
            Result result = db.query(query);
            result.setStatus(Result.STATUS_OK);
            long tEndInd = System.nanoTime();
            long elapsedTimeInd = tEndInd - tStartInd;
            double secondsInd = elapsedTimeInd / 1.0E09;
            result.setTime(secondsInd);
            System.out.println("Query completed in " + secondsInd + " secs.");
            result.prepare();
            if (returnField != null) {
                s = toJSON(result.getUniqueRecords(returnField));
            } else {
                s = toJSON(result.getRecords());
            }
            if (callback != null) {
                s = callback + " ( " + s + " ) ";
            }
            //System.out.println(ss);
        } else {
            s = toJSON(Result.STATUS_STORE_NOT_FOUND);
        }
       return s;
    }
    
    
}
