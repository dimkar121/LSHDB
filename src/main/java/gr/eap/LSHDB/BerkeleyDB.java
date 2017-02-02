/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import com.sleepycat.je.DatabaseException;
import gr.eap.LSHDB.util.FileUtil;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class BerkeleyDB implements StoreEngine {
    final static Logger log = Logger.getLogger(BerkeleyDB.class);

    Database db;
    Environment dbEnv;

    public BerkeleyDB(String folder, String storeName, String entity,boolean massInsertMode) {
        try {
            // create a configuration for DB environment
            EnvironmentConfig envConf = new EnvironmentConfig();
            // environment will be created if not exists
            envConf.setAllowCreate(true);

            // open/create the DB environment using config
            dbEnv = new Environment(new File(folder), envConf);

            // create a configuration for DB
            DatabaseConfig dbConf = new DatabaseConfig();
            // db will be created if not exits
            dbConf.setAllowCreate(true);           
            db = dbEnv.openDatabase(null, storeName + "-" + entity, dbConf);

        } catch (DatabaseException ex) {
            log.error("Error opening BerkleleyDB store "+ storeName );
        }
    }
    
    public long count(){
       try{ 
        return db.count();
       }catch(DatabaseException ex){
          log.error("Error counting BerkleleyDB store.");             
       } 
      return 0; 
    }

    

    public void set(String key, Object data) {
        try {
            DatabaseEntry dbKey = new DatabaseEntry(key.getBytes("UTF-8"));
            //System.out.println(serialize(data));
            DatabaseEntry dbData = new DatabaseEntry(FileUtil.serialize(data));
            if ((db.put(null, dbKey, dbData)
                    == OperationStatus.SUCCESS)) {

            } else {
                log.error("Failed putting for " + key);
            }
        } catch (DatabaseException | IOException ex) {
            log.error("Error setting BerkeleyDB key " + key);

        }

    }

    public void close() {
        try {
            db.close();
            dbEnv.close();
        } catch (DatabaseException ex) {
             log.error("Error closing BerkeleyDB store.");
        }
    }

    
    
   

    public Object get(String key) {
        try {
            DatabaseEntry dbKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry dbData = new DatabaseEntry();

            //StringBinding.stringToEntry(key, dbKey);
            if ((db.get(null, dbKey, dbData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS)) {
                return FileUtil.deserialize(dbData.getData());
            }
        } catch (DatabaseException | IOException | ClassNotFoundException ex) {
            log.error("Error getting BerkeleyDB key " + key);
        }
        return null;
    }

    public boolean contains(String key) {
        try {
            DatabaseEntry dbKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry dbData = new DatabaseEntry();

            //StringBinding.stringToEntry(key, dbKey);
            if ((db.get(null, dbKey, dbData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS)) {
                return true;
            } else {
                return false;
            }
        } catch (DatabaseException | IOException  ex) {
              log.error("Error getting (contains) BerkeleyDB key " + key);
        }
        return false;
    }

    
    
    
      public  Iterable createIterator(){
          return new BerkeleyDBIterator(db);
      }
   
      
     
    
      
       
      
    
    

}
