/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author dimkar
 */
public class Property {

        
    public static String get(String fileName, String key) {
        FileInputStream in;
        Properties props = new Properties();    
        try {
            in = new FileInputStream("." + File.separator + fileName);
            props.load(in);           
            in.close();
            return props.getProperty(key);
        } catch (Exception ex) {
            System.out.println("Properties file not found. Default values will be used.");
        }
        return null;
    }
    
    
    
    
     public static String[] getList(String fileName, String key) {
        FileInputStream in;
        Properties props = new Properties();    
        try {
            in = new FileInputStream("." + File.separator + fileName);
            props.load(in);           
            in.close();
            return props.getProperty(key).split(",");           
        } catch (Exception ex) {
            System.out.println("Properties file not found. Default values will be used.");
        }
        return null;
    }
    
    
}
