/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Property;
import java.util.Properties;

/**
 *
 * @author dimkar
 */
public class BuildDB {

    public static DataFactory build(String folder, String dbName, String file, String dbEngine) {
        try {
            Class c = Class.forName(dbEngine);
            DataFactory db = (DataFactory) c.getConstructor(String.class,String.class,String.class).newInstance(folder,dbName,file);
            
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
