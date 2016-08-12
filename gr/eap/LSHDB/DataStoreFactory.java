/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author dimkar
 */
public class DataStoreFactory {

    public static StoreEngine build(String folder, String dbName, String file, String dbEngine,boolean massInsertMode) throws ClassNotFoundException, NoSuchMethodException {
        try {
            Class c = Class.forName(dbEngine);
            StoreEngine db = (StoreEngine) c.getConstructor(String.class,String.class,String.class,boolean.class).newInstance(folder,dbName,file,massInsertMode);
            
            return db;
        //} catch (ClassNotFoundException ex) {
         //   System.err.println(ex + " DataFactory class must be in class path.");
        } catch (InstantiationException ex) {
            System.err.println(ex + " DataFacory class must be concrete.");
        } catch (IllegalAccessException ex) {
            System.err.println(ex + " DataFactory class must have a no-arg constructor.");
        }
        catch (InvocationTargetException ex){
            ex.printStackTrace();
        }
        return null;
    }

}
