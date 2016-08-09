/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

/**
 *
 * @author dimkar
 */
public class LSHStoreFactory {

     public static Configuration build(String folder, String dbName, String LSHConf, String dbEngine, boolean massInsertMode) {
        try {
            Class c = Class.forName(LSHConf);
            Configuration db = (Configuration) c.getConstructor(String.class, String.class, String.class, boolean.class).newInstance(folder, dbName, dbEngine, massInsertMode);            
            return db;
        } catch (ClassNotFoundException ex) {
            System.err.println(ex + " DataFactory class must be in class path.");
        } catch (InstantiationException ex) {
            System.err.println(ex + " DataFacory class must be concrete.");
        } catch (IllegalAccessException ex) {
            System.err.println(ex + " DataFactory class must have a no-arg constructor.");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    
    
    public static DataStore build(String folder, String dbName, String LSHStore, String dbEngine, Configuration conf, boolean massInsertMode) {
        try {
            Class c = Class.forName(LSHStore);
            DataStore db = (DataStore) c.getConstructor(String.class, String.class, String.class, Configuration.class, boolean.class).newInstance(folder, dbName, dbEngine, conf, massInsertMode);
            
            return db;
        } catch (ClassNotFoundException ex) {
            System.err.println(ex + " DataFactory class must be in class path.");
        } catch (InstantiationException ex) {
            System.err.println(ex + " DataFacory class must be concrete.");
        } catch (IllegalAccessException ex) {
            System.err.println(ex + " DataFactory class must have a no-arg constructor.");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

}
