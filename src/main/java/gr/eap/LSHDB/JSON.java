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
    public static String MAX_NO_RESULTS = "maxNoRes";
    public static String RETURNED_FIELD = "returnField";
    public static String SIMILARITY_SLIDING_PERCENTAGE = "simPer";
    public static String CALLBACK = "callback";
    public static String ERROR = "error";
    public static String ERROR_MSG = "errorMessage";    
    
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

    public HashMap parseError(String msg, int error) {
        HashMap m = new HashMap();
        m.put(ERROR, error);
        m.put(ERROR_MSG, msg);        
        return m;
    }

    public String getDbName(String msg) {
        msg = msg.replaceAll(JSON_REQUEST, "");
        String dbName = msg.substring(0, msg.indexOf("?"));
        return dbName;
    }

    public String convert(String msg, DataStore db) {
        String s = "";
        msg = msg.replaceAll(JSON_REQUEST, "");
        String dbName = msg.substring(0, msg.indexOf("?"));
        msg = msg.replaceAll(dbName, "");
        msg = msg.replaceAll("\\?", "");
        msg = msg.substring(0, msg.indexOf(' '));
        String callback = URLUtil.getRequestKey(msg, CALLBACK);
        String returnField = URLUtil.getRequestKey(msg, RETURNED_FIELD);
        try {
            int simPercentage = 100;
            try {
                simPercentage = Integer.parseInt(URLUtil.getRequestKey(msg, SIMILARITY_SLIDING_PERCENTAGE));
            } catch (NumberFormatException ex) {
                simPercentage = 100;
            }
            int maxNoResults = 20;
            try {
                maxNoResults = Integer.parseInt(URLUtil.getRequestKey(msg, MAX_NO_RESULTS));
            } catch (NumberFormatException ex) {
                maxNoResults = 20;
            }

            QueryRecord query = new QueryRecord(dbName, maxNoResults);
            double sim = simPercentage * 1.0 / 100;

            Map<String, String> m = URLUtil.splitQuery(msg);

            if (db != null) {
                if (m.entrySet().size() > 0) {
                    Iterator it = m.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        query.set(pair.getKey() + "", pair.getValue());

                        if (db.getConfiguration().isKeyed) {
                            String value = (String) pair.getValue();
                            //System.out.println("name="+pair.getKey()+" value=" + value + ".");
                            String[] values = value.split(" ");

                            query.set(pair.getKey() + "", value, sim, true);
                            query.set(pair.getKey() + Key.TOKENS, values, sim, true);
                        } else {
                            query.set(pair.getKey() + "", pair.getValue());
                        }

                        //System.out.println(pair.getKey() + " = " + pair.getValue());
                    }
                } else {
                    throw new JSONException(Result.NO_QUERY_VALUES_SPECIFIED_ERROR_MSG, Result.NO_QUERY_VALUES_SPECIFIED);
                }
                if (!db.getConfiguration().isKeyed) {
                    query.set(sim, true);
                }

                long tStartInd = System.nanoTime();
                 Result result=null;
                try{
                   result = db.query(query);
                }catch(NoKeyedFieldsException ex){
                     throw new JSONException(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG, Result.NO_KEYED_FIELDS_SPECIFIED);
                }   
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

                //System.out.println(ss);
            } else {
                throw new JSONException(Result.STORE_NOT_FOUND_ERROR_MSG, Result.STORE_NOT_FOUND);
            }
            if (callback != null) {
                s = callback + " ( " + s + " ) ";
            }
        } catch (JSONException ex) {
            HashMap h = parseError(ex.getMessage(), ex.error);
            s = toJSON(h);
            System.out.println(ERROR + ": " + ex.getMessage());
            if (callback != null) {
                s = callback + " ( " + s + " ) ";
            }
            return s;
        }
        return s;
    }

}
