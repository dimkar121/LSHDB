/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

import gr.eap.LSHDB.HammingConfiguration;
import gr.eap.LSHDB.HammingKey;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.Record;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Eap {

    public static void main(String[] args) {
       try { 
        String folder = "c:/MAPDB";
        String dbName = "eap";
        String engine = "gr.eap.LSHDB.MapDB";
        //HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, 30, 140, 0.1, 1200, null);
         Key key1 = new HammingKey("recordLevel", 30, .1, 110, 1000, true, true);
        //Key key2 = new HammingKey("title", 30, .1, 55, 500, true, true);
        //Key key1 = new HammingKey("recordLevel", 30, .1, 110, 1000, false, true);

        HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1},true);
        hc.saveConfiguration();
        //HammingConfiguration hc = new HammingConfiguration("c:/tmp", "testBer", 30, 105, 0.1, 1200);
        //HammingLSHDB lsh = new HammingLSHStore("c:/tmp", "testBer", hc);
        HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc,true);

        
        
            // String file = "c://voters//test_voters_A.txt";


            //for (int j = 9; j < 13; j++) {
              
                String fileName = "c://voters//eap//dataf.csv";
               
        

                //FileReader input1 = new FileReader(file);
                //BufferedReader bufRead1 = new BufferedReader(input1);
                
                BufferedReader bufRead1 = new BufferedReader(
		   new InputStreamReader(
                      new FileInputStream(new File(fileName)), "UTF8"));
                
                
                int lines = FileUtil.countLines(fileName);
                Random r = new Random();
                for (int i = 0; i < lines; i++) {
                    String line1 = bufRead1.readLine();
                    StringTokenizer st1 = new StringTokenizer(line1, ",");
                    String year = st1.nextToken();
                    String afm = st1.nextToken();                    
                    String lastName = st1.nextToken();
                    String firstName = st1.nextToken();
                    String fatherName = st1.nextToken();
                    String street = st1.nextToken();
                    String no = st1.nextToken();                    
                    String town = "";
                    if (st1.hasMoreTokens())
                         town = st1.nextToken();
                    String prog = "";
                    if (st1.hasMoreTokens())
                         prog = st1.nextToken();
          
                    
                    
                    String id = year+"_"+i;

                    Record rec = new Record();
                    rec.setId(id);
                    rec.set("First Name", firstName);
                    rec.set("Last Name", lastName);
                    rec.set("Father Name", fatherName);    
                    rec.set("Address", street + " " + no);
                    rec.set("Programme", prog);                    
                    rec.set("SSN", afm);
                        
                    rec.set("Year", year);
                    rec.set("Town", town);
                     

                    
                      System.out.println(i);
                      lsh.insert(rec);
                      

                }
                
                bufRead1.close();                
                lsh.close();
            //}
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
       
    }
}
