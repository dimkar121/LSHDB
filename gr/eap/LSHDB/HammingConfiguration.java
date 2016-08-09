/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Property;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author dimkar
 */
public class HammingConfiguration extends Configuration implements Serializable {
    
    
    
    public HammingConfiguration(String folder, String dbName, String dbEngine, boolean massInsertMode) {
        super(folder, dbName, dbEngine,massInsertMode);  

    }

    public HammingConfiguration(String folder, String dbName, String dbEngine, Key[] keysList,boolean massInsertMode) {
        super(folder, dbName, dbEngine,keysList,massInsertMode);
    }
    
    
    
     public static void main(String[] args) {
        String folder = "c:/MAPDB";       
        String dbName = "dblp";
        String engine = "gr.eap.LSHDB.MapDB";
        Key key1 = new HammingKey("author", 30, .1, 55, 500, true, true);
        HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1},false);
     }   
    
    
    
}
