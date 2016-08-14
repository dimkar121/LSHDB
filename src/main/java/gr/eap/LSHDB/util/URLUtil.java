/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author dimkar
 */
public class URLUtil {

    public static Map<String, String> splitQuery(String url) {
        try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            String[] pairs = url.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                if (key.endsWith(QueryRecord.QUERY_VALUE)){
                      key = key.replace(QueryRecord.QUERY_VALUE,"");
                      query_pairs.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
                }      
            }
            return query_pairs;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
     public static String getRequestKey(String url,String requestKey) {
        try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            String[] pairs = url.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                if (key.equals(requestKey)) 
                    return value;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    
}
