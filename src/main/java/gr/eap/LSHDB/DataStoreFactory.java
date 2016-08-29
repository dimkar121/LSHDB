/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import static gr.eap.LSHDB.StoreEngineFactory.log;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class DataStoreFactory {
    final static Logger log = Logger.getLogger(DataStoreFactory.class);

     public static Configuration build(String folder, String dbName, String LSHConf, String dbEngine, boolean massInsertMode) throws ClassNotFoundException, NoSuchMethodException {
        try {
            Class c = Class.forName(LSHConf);
            Configuration db = (Configuration) c.getConstructor(String.class, String.class, String.class, boolean.class).newInstance(folder, dbName, dbEngine, massInsertMode);            
            return db;
        }  catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            log.error(LSHConf + " Initialization problem of LSHConf "  ,ex);
        }
        return null;
    }
    
    
    
    public static DataStore build(String folder, String dbName, String LSHStore, String dbEngine, Configuration conf, boolean massInsertMode) throws ClassNotFoundException, NoSuchMethodException{
        try {
            Class c = Class.forName(LSHStore);
            DataStore db = (DataStore) c.getConstructor(String.class, String.class, String.class, Configuration.class, boolean.class).newInstance(folder, dbName, dbEngine, conf, massInsertMode);
            
            return db;
        }  catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            log.error(LSHStore + " Initialization problem of LSHStore "  ,ex);
        }
        return null;
    }

        
    
    
}
