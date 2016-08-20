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

    String request;
    String dsName;
    String returnField;
    String callBack;

    public String getStoreName() {
        return dsName;
    }
    double sim;

    public double getSimilarity() {
        return sim;
    }
    Map<String, String> requestKeys;

    public Map<String, String> getRequestKeys() {
        return requestKeys;
    }

    public JSON(String request) {
        this.request = request;
    }

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

    public String buildStoreName() {
        request = request.replaceAll(JSON_REQUEST, "");
        String dsName = request.substring(0, request.indexOf("?"));
        return dsName;
    }

    public String prepare(Result result) {
        result.prepare();
        String s = "";
        if (returnField != null) {
            s = toJSON(result.getUniqueRecords(returnField));
        } else {
            s = toJSON(result.getRecords());
        }
      return s;   
    }

    
    
    public QueryRecord buildQueryRecord() throws JSONException {
        String storeName = buildStoreName();
        String s = "";
        request = request.replaceAll(storeName, "");
        request = request.replaceAll("\\?", "");
        request = request.substring(0, request.indexOf(' '));
        callBack = URLUtil.getRequestKey(request, CALLBACK);
        returnField = URLUtil.getRequestKey(request, RETURNED_FIELD);
        int simPercentage = 100;
        try {
            simPercentage = Integer.parseInt(URLUtil.getRequestKey(request, SIMILARITY_SLIDING_PERCENTAGE));
        } catch (NumberFormatException ex) {
            simPercentage = 100;
        }
        int maxNoResults = 20;
        try {
            maxNoResults = Integer.parseInt(URLUtil.getRequestKey(request, MAX_NO_RESULTS));
        } catch (NumberFormatException ex) {
            maxNoResults = 20;
        }
        QueryRecord query = new QueryRecord(storeName, maxNoResults);
        sim = simPercentage * 1.0 / 100;
        requestKeys = URLUtil.splitQuery(request);
        return query;
    }

    

}
