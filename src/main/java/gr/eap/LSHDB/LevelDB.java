/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.FileUtil;
import java.io.File;

import java.io.*;

import org.apache.log4j.Logger;
import org.fusesource.leveldbjni.JniDBFactory;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

/**
 *
 * @author dimkar
 */
public class LevelDB implements StoreEngine {

    final static Logger log = Logger.getLogger(LevelDB.class);

    public String pathToDB;
    public DB db;
    String fileName;
    boolean bulkInsert = false;
    WriteBatch batch;
    int bulkCounter = 0;
    
    public LevelDB(String folder, String storeName, String entity, boolean massInsertMode) {
        pathToDB = folder + System.getProperty("file.separator") + storeName;
        File theDir = new File(pathToDB);
        if (!theDir.exists()) {
            log.info("Path to the store " + pathToDB + " not found. A new store will be created.");
            theDir.mkdir();
        }
        fileName = pathToDB + System.getProperty("file.separator") + entity;
        Options options = new Options();        
        options.createIfMissing(true);
        options.compressionType(CompressionType.NONE);                
        try {
            options.cacheSize(600 * 1048576); // 600MB cache                        
            JniDBFactory.pushMemoryPool(1024 * 512);
            db = factory.open(new File(fileName), options);            
            if (massInsertMode){
                bulkInsert = true;
                batch = db.createWriteBatch();
            }    
                    
        } catch (IOException ex) {
            log.error("Error opening LevelDB store "+ storeName );
        }
    }

    public void close() {
        try {
            if (bulkInsert){                   
                db.write(batch);
              batch.close();
           }    
           db.close();
           JniDBFactory.popMemoryPool();
        } catch (IOException ex) {
             log.error("Error closing LevelDB store." );
        }

    }

    public synchronized Object get(String key) {
        try {           
            return FileUtil.deserialize(db.get(bytes(key)));
        } catch (DBException | IOException | ClassNotFoundException ex) {
           log.error("Error getting LevelDB  key "+key);
        }
        return null;
    }

    public synchronized boolean contains(String key) {
        try {           
            if (db.get(bytes(key)) != null)
                return true;
        } catch (DBException ex) {
          log.error("Error getting (contains) LevelDB  key "+key);
        }
        return false;
    }

    public synchronized void set(String key, Object data) {
        try {
            if (bulkInsert){
                bulkCounter++;
                batch.put(bytes(key), FileUtil.serialize(data));
                if (bulkCounter == 1000000){
                  bulkCounter = 0;
                  db.write(batch);
                  batch = db.createWriteBatch();                 
                }  
            }    
            else 
              db.put(bytes(key), FileUtil.serialize(data));              
        } catch (IOException ex) {
           log.error("Error setting LevelDB  key "+key);
        }
    }

    public long count() {
        return 0;
    }

 
   
 public  Iterable createIterator(){
          return new LevelDBIterator(this);
      } 
    
   
}
