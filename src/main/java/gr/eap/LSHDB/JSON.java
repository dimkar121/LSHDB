/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.URLUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author dimkar
 */
public class JSON {

    public final static String JSON_REQUEST = "/JSON/";
    public final static String MAX_NO_RESULTS = "maxNoRes";
    public final static String RETURNED_FIELD = "returnField";
    public final static String SIMILARITY_SLIDING_PERCENTAGE = "simPer";
    public final static String CALLBACK = "callback";
    public final static String ERROR = "error";
    public final static String ERROR_MSG = "errorMessage";
    public final static String ALIAS = "alias";
    public final static String STATUS = "status";
    public final static String STATUSMAP = "statusMap";
    public final static String DATA = "data";
    

    String request;
    String storeName;
    String returnField;
    String callBack;

    public String getCallBack(){
        return callBack;
    }
    
    public String getStoreName() {
        return storeName;
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

    public HashMap prepareError(int error, String msg) {
        HashMap m = new HashMap();
        m.put(ERROR, error);
        m.put(ERROR_MSG, msg);
        return m;
    }

    public void setStoreName() {
        request = request.replaceAll(JSON_REQUEST, "");
        storeName = request.substring(0, request.indexOf("?"));
    }

    public String prepare(Result result) {
        result.prepare();
        String response = "";
        ArrayList<Record> data =  result.getRecords();
        if (returnField != null) 
             data = result.getUniqueRecords(returnField);

        Set<String> aliases = result.getStatusMap().keySet();
        ArrayList<HashMap> arr=new ArrayList<HashMap>();
        for (String alias:aliases){
            HashMap m = new HashMap();
            m.put(ALIAS,alias);
            m.put(STATUS,result.getStatusMap().get(alias));            
            arr.add(m);
        }
        
        HashMap map = new HashMap();
        map.put(DATA,data);
        map.put(STATUSMAP,arr);
        response = toJSON(map);
        return response;
    }

    
    
    public QueryRecord buildQueryRecord()  {
        setStoreName();
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
