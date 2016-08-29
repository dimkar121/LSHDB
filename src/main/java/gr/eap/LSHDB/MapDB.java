/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.volume.Volume;

/**
 *
 * @author dimkar
 */
public class MapDB implements StoreEngine {
final static Logger log = Logger.getLogger(MapDB.class);
    
    public String pathToDB;
    public DB db;
    public DB mem;
    HTreeMap map;
    HTreeMap mapDisk;
    Volume memVolume;
    Volume fileVolume;    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    double prop = .1;
    String fileName;

 

    public MapDB(String storeName, String entity){
        db = DBMaker.memoryDB().make();                    
        mem = DBMaker.memoryDB().make();            
        map = mem.hashMap(entity).create();   
        mapDisk = db.hashMap(entity).create();
    }

    

   

    public MapDB(String folder, String dbName, String entity, boolean massInsertMode) {
            pathToDB = folder + System.getProperty("file.separator") + dbName;
            File theDir = new File(pathToDB);
            if (!theDir.exists()) {
                log.info("Path to the store "+pathToDB+" not found. A new store will be created.");
                theDir.mkdir();
            }
            fileName = pathToDB + System.getProperty("file.separator") + entity;
            if (massInsertMode) {
                db = DBMaker.fileDB(fileName).fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable().checksumHeaderBypass().closeOnJvmShutdown().make();
                mem = DBMaker.memoryDB().make();

            } else {
                 db = DBMaker.fileDB(fileName).fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable().checksumHeaderBypass().closeOnJvmShutdown().make();
                 mem = DBMaker.memoryDB().make();
            }
             mapDisk = db.hashMap(entity).createOrOpen();
             map = mem.hashMap(entity).expireAfterGet(1, TimeUnit.SECONDS).expireExecutor(Executors.newScheduledThreadPool(2)).create();
    }

    public void close() {
        map.close();
        mapDisk.close();
    }

    public synchronized Object get(String key) {
        try {
            if (map.containsKey(key)) {
                Object o = map.get(key);
                return o;
            } else if (mapDisk.containsKey(key)) {
                Object o = mapDisk.get(key);
                //map.put(key,o);                    
                return o;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public synchronized boolean contains(String key) {
         if (map.containsKey(key)) {
           return true;
        }
        if (mapDisk.containsKey(key)) {
            if (! map.containsKey(key))
              map.put(key, mapDisk.get(key));
            return true;
        }
        return false;
    }

    public synchronized void set(String key, Object data) {
       mapDisk.put(key, data);
       // map.put(key, data);
    }

    public synchronized void persistentSet(String key, Object data) {
        mapDisk.put(key, data);
    }

    public synchronized boolean persistentContains(String key) {

        if (mapDisk.containsKey(key)) {
            return true;
        }
        return false;
    }

    public long count() {
        return mapDisk.size();
    }
}
