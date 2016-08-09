/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Record;
import java.util.ArrayList;

/**
 *
 * @author dimkar
 */
public interface DataFactory {
     
     public void set(String key, Object data);
     public Object get(String key);
     public boolean contains(String key);
     public abstract ArrayList<Record> browse(int rowCount, int pageNo, String key);
     public abstract ArrayList<Record> browseBack(int rowCount, int pageNo,String key);    
     public abstract long count();
     public void close();
 }
