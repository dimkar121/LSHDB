/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.embeddables.StringEmb;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import javafx.event.Event;

/**
 *
 * @author dimkar
 */
public class GramStore extends DataStore {

    GramConfiguration hConf;

    HashMap<String, Integer> bigramMap = new HashMap();

    class TimesComparator implements Comparator<String> {

        @Override
        public int compare(String x, String y) {
            //compares its two arguments for order.  Returns a negative integer,
            // zero, or a positive integer as the first argument is less than, equal
           //to, or greater than the second.<p>
            int a = Integer.parseInt(x.split("_")[0]);
            int b = Integer.parseInt(y.split("_")[0]);
            if (a < b) 
                return 1;
            if (a ==  b)
               return 0;
           return -1;            
        }
    }

    public GramStore(String folder, String dbName, String dbEngine, Configuration hc, boolean massInsertMode) throws StoreInitException {
        this.folder = folder;
        this.storeName = dbName;
        this.massInsertMode = massInsertMode;
        if (hc == null) {
            hConf = new GramConfiguration(folder, dbName, dbEngine, massInsertMode);
        } else {
            hConf = (GramConfiguration) hc;
        }
        init(dbEngine, massInsertMode);
    }

    public Configuration getConfiguration() {
        return this.hConf;
    }

    @Override
    public void setHashKeys(String id, Embeddable emb, String keyFieldName) {
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        StoreEngine hashKeys = keys;
        if (isKeyed) {
            hashKeys = this.getKeyMap(keyFieldName);
        }
        HashMap<String, ArrayList<String>> cache = this.getCacheMap(keyFieldName);

        Key key = this.getConfiguration().getKey(keyFieldName);
        int q = ((GramKey) key).q;

        for (int j = 0; j < key.L; j++) {

            String s = ((StringEmb) emb).getValue();
            ArrayList<String> gramList = ((StringEmb) emb).getQGrams();
            String idLen = id + "//" + s.length();
            for (int i = 0; i < gramList.size(); i++) {
                String hashKey = gramList.get(i);
                //hashKeys.set(hashKey + "_" + tt, id);
                if (cache.containsKey(hashKey)) {
                    ArrayList<String> arr = cache.get(hashKey);
                    if (arr.size() < getCacheEntryLimit()) {
                        arr.add(idLen);
                    } else if (arr.size() == getCacheEntryLimit()) {
                        long tt = incId();
                        hashKeys.set(hashKey + "_" + tt, arr);
                        cache.remove(hashKey);
                    }
                } else {
                    ArrayList<String> arr = new ArrayList<String>();
                    arr.add(idLen);
                    cache.put(hashKey, arr);
                }

            }
        }
    }

    @Override
    public String buildHashKey(int j, Embeddable emb, String keyFieldName) {
        return "";
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public boolean distance(Embeddable struct1, Embeddable struct2, Key key) {

        String str1 = ((StringEmb) struct1).getValue();
        String str2 = ((StringEmb) struct2).getValue();
        int editDistanceThreshold = ((GramKey) key).t;
        int q = ((GramKey) key).q;
        int times = ((GramKey) key).times;

        int size1 = str1.length();    //((StringEmb) struct1).getQGrams().size(); 
        int size2 = str2.length();     //((StringEmb) struct1).getQGrams().size();

        int gramThreshold = Math.max(size1, size2) - q + q - 1 - editDistanceThreshold * q;
        //if (times != cardinality)
        //  System.out.println(str1 + " vs " + str2 + "  times="+times );
        if (times >= gramThreshold) {   // count filtering

            /*
             int[][] distance = new int[str1.length() + 1][str2.length() + 1];

             for (int i = 0; i <= str1.length(); i++) {
             distance[i][0] = i;
             }
             for (int j = 1; j <= str2.length(); j++) {
             distance[0][j] = j;
             }

             for (int i = 1; i <= str1.length(); i++) {
             for (int j = 1; j <= str2.length(); j++) {
             distance[i][j] = min(
             distance[i - 1][j] + 1,
             distance[i][j - 1] + 1,
             distance[i - 1][j - 1] + ((str1.charAt(i - 1) == (str2.charAt(j - 1)) ? 0 : 1))
             );
             }
             }

             int d = distance[str1.length()][str2.length()]; */
            //System.out.println(str1+" "+str2+" d="+d+" gt="+gramThreshold);
            return true;
        }
        return false;
    }

    @Override
    public Result forkHashTables(Embeddable struct1, final QueryRecord queryRec, String keyFieldName) {
        final Configuration conf = this.getConfiguration();
        final int maxQueryRows = queryRec.getMaxQueryRows();
        final boolean performComparisons = queryRec.performComparisons(keyFieldName);
        final double userPercentageThreshold = queryRec.getUserPercentageThreshold(keyFieldName);
        final StoreEngine keys = this.getKeyMap(keyFieldName);
        final StoreEngine data = this.getDataMap(keyFieldName);
        Key key = conf.getKey(keyFieldName);
        boolean isPrivateMode = conf.isPrivateMode();
        int q = ((GramKey) key).q;
        char padChar = ((GramKey) key).padChar;
        final String keyFieldName1 = keyFieldName;
        final Embeddable struct11 = struct1;

        final Key newKey = key.create(userPercentageThreshold);

        final Result result = new Result(queryRec);

        String queryString = ((StringEmb) struct11).getValue();

        ArrayList<String> gramList = ((StringEmb) struct11).getQGrams();
        int editDistanceThreshold = ((GramKey) key).t;

        HashMap freq = new HashMap();

        int lower = queryString.length() - 2 - editDistanceThreshold;
        int upper = queryString.length() - 2 + editDistanceThreshold;
        System.out.println("lower=" + lower + " upper=" + upper + " q.len=" + (queryString.length() - 2) + " q=" + queryString + " grams=" + gramList);
        for (int o = lower; o <= upper; o++) {
            HashMap<String, Integer> map1 = new HashMap<String, Integer>();
            freq.put(o + "_cand", map1);
            HashMap<String, Integer> map2 = new HashMap<String, Integer>();
            freq.put(o + "_succ", map2);
        }
        Comparator<String> comparator = new TimesComparator();
        PriorityQueue<String> pq = new PriorityQueue<String>(10, comparator);
            
        
        String hashKey = "";

        for (int i = 0; i < gramList.size(); i++) {
            hashKey = gramList.get(i);
            int cc = 0;
            Iterable iterator = keys.createIterator();
            System.out.println("gram=" + hashKey);
            for (iterator.seek(hashKey); iterator.hasNext(); iterator.next()) {
                String nextKey = iterator.getKey();
                if (nextKey.startsWith(hashKey)) {
                    cc++;
                    ArrayList<String> arr = null;
                    try {
                        arr = (ArrayList<String>) iterator.getValue();
                        //id = iterator.getValue() + "";
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    for (int j = 0; j < arr.size(); j++) {

                        String content = arr.get(j);

                        String id = content.split("//")[0];
                        String lens = content.split("//")[1];
                        int len = Integer.parseInt(lens) - q;

                        if (Math.abs(len - (queryString.length() - 2)) > editDistanceThreshold) //length filtering                          
                            continue;
                       

                        if (freq.containsKey(len + "_cand")) {

                            HashMap<String, Integer> map1 = (HashMap) freq.get(len + "_cand");
                            HashMap<String, Integer> map2 = (HashMap) freq.get(len + "_succ");
                           

                            if (map1.containsKey(id)) {
                                int times = map1.get(id);
                                times++;
                                map1.put(id,times);
                            } else 
                                map1.put(id, 1);
                            
                            
                            /*
                            if ((map1.containsKey(id)) && (!pq.contains(id))) {
                                int times = map1.get(id);
                                times++;
                                int gramThreshold = Math.max(queryString.length() - q, len) + q - 1 - editDistanceThreshold * q;
                                if (times >= gramThreshold) {
                                    pq.add(times + "_" + id); // 
                                    map1.remove(id);
                                } else {
                                    map1.put(id, times);
                                }
                            }
                            if (!map1.containsKey(id) && (!pq.contains(id))) {
                                map1.put(id, 1);
                            }*/
                            
                        }                        

                    }

                } else {
                    break;
                }
            }

            iterator.close();
        }

        Iterator it0 = freq.entrySet().iterator();
        while (it0.hasNext()) {
            Map.Entry pair = (Map.Entry)it0.next();
            String key1 =(String) pair.getKey();
            if (key1.endsWith("_cand")) {
                HashMap<String,Integer> map1 =(HashMap) freq.get(key1);
                for (String id : map1.keySet()) { 
                        int times = map1.get(id);
                        pq.add(times + "_" + id);
                }                
            }
        }    
        
        
        
            
        Iterator it = freq.entrySet().iterator();
        //while (it.hasNext()) {
          //  Map.Entry pair = (Map.Entry)it.next();
            //String key1 =(String) pair.getKey();
            //if (key1.endsWith("_succ")) {
                
                while(!pq.isEmpty()){                        
                    String entry = ((String) pq.poll());
                    
                    String id = entry.split("_")[1];
                    String times = entry.split("_")[0];
                    CharSequence cSeq = Key.KEYFIELD;
                    String idRec = id;
                    if (idRec.contains(cSeq)) {
                        idRec = id.split(Key.KEYFIELD)[0];
                    }
                    Record dataRec = (Record) records.get(idRec);   // which id and which record shoudl strip the "_keyField_" part , if any
                    result.add(keyFieldName1, dataRec);
                    int matchesNo = result.getDataRecordsSize(keyFieldName1);
                    if (matchesNo >= maxQueryRows) {
                        return result;
                    }
                }
            //}
        //}

        return result;
    }

}
