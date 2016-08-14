/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author dimkar
 */
public class RecordList implements Serializable{
     
    public static final long serialVersionUID = 888L;
    
    int maxQueryRows;
    boolean performComparisons;    
    transient HashMap<String, Record> m = new HashMap<String, Record>();
    transient Map<String, Integer> freq = new HashMap<String, Integer>();
    String fieldName;
    int elements = 0;
    
    transient PriorityQueue<Collision> pq = new PriorityQueue<Collision>();
     
    public HashMap<String, Record> getMap(){
        return m;
    }
    
    public RecordList(String fieldName, int maxQueryRows, boolean performComparisons){
        this.fieldName = fieldName;
        this.performComparisons = performComparisons;     
        this.maxQueryRows = maxQueryRows;
    }
      
    
    public int getDataRecordsSize() {       
            return m.size();
    }
    
    public boolean contains(String Id){
        if (m.containsKey(Id))
            return true;
        return false;
    }
    
    public boolean add(Record rec) {        
        if (performComparisons) {
            if (!m.containsKey(rec.getId())) {
                m.put(rec.getId(), rec);              
                elements++;
                return true;
            }
        } else {
            
            /*
            Collision c = new Collision(rec, 0);
            if (pq.contains(c)) {
                Collision c1 = pq.remove();
                c1.collisions = c1.collisions + 1;
                pq.add(c1);
            } else {
                pq.add(new Collision(rec, 1));
            }*/

            if (freq.containsKey(rec.getId())) {
                int cols = freq.get(rec.getId());
                cols++;
                freq.put(rec.getId(), cols);
                
                
            } else {
                freq.put(rec.getId(), 1);
               
            }
            if (! m.containsKey(rec.getId())) {
                 elements++;
                m.put(rec.getId(), rec);
            }

            return true;
        }
        return false;
    }

    
    
    public HashMap<String, Record> getRecords() {
        //ArrayList<Record> arr = new ArrayList<Record>();
        HashMap<String, Record> map = new HashMap<String, Record>();
         
            
        if (performComparisons) {
            Iterator it = m.entrySet().iterator();
            while ((it.hasNext()) && (map.size() < maxQueryRows)) {
                Map.Entry pair = (Map.Entry) it.next();
                map.put((String) pair.getKey(),(Record) pair.getValue());
                //arr.add((Record) pair.getValue());
                //it.remove();
            }
        } else {
            
            /*
            while ((!pq.isEmpty()) && (arr.size() < maxQueryRows)) {
                Collision c = pq.poll();
                //System.out.println("Collisions " + c.Id + " " + c.collisions);
                arr.add(c.getRecord());
            }
            pq.clear();*/
          
            List<Map.Entry<String, Integer>> list = findTop(freq, maxQueryRows);
            
            for (int i=list.size()-1;i>=0;i--){
            
               //for (Map.Entry e : list) {
                Map.Entry e = (Map.Entry) list.get(i);
                //System.out.println("HashMap " + e.getKey() + " " + e.getValue());
                Record r = m.get(e.getKey());
                map.put((String) e.getKey(), r);
                //arr.add(r);           
                // ....
            }

        }
        //return arr;
        return map;
    }
    
    
    
    
    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
            findTop(Map<K, V> map, int n) {
        Comparator<? super Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
                V v0 = e0.getValue();
                V v1 = e1.getValue();

                //int v0 =(Integer) e0.getValue();
                //int v1 =(Integer) e1.getValue();
                //return v0 - v1;            
                return v0.compareTo(v1);
            }
        };

        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<Map.Entry<K, V>>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }

        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    
    
}
