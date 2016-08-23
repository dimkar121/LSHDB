/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class StoreEngineFactory {
final static Logger log = Logger.getLogger(StoreEngineFactory.class);
    
    public static StoreEngine build(String folder, String dbName, String file, String dbEngine,boolean massInsertMode) throws ClassNotFoundException, NoSuchMethodException  {
        try {
            Class c = Class.forName(dbEngine);
            StoreEngine db = (StoreEngine) c.getConstructor(String.class,String.class,String.class,boolean.class).newInstance(folder,dbName,file,massInsertMode);
            
            return db;         
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            log.error(dbEngine + " Initialization problem of DataStore "  ,ex);
        }
        return null;
    }

}
