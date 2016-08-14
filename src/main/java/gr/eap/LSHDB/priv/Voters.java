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
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Voters {

    public static void main(String[] args) {
       try{ 
        String folder = "c:/MAPDB";
        String dbName = "voters";
        String engine = "gr.eap.LSHDB.MapDB";

        //HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, 30, 140, 0.1, 1200, null);
        //Key key1 = new HammingKey("author", 30, .1, 55, 500, true, true);
        //Key key2 = new HammingKey("title", 30, .1, 55, 500, true, true);
        Key key1 = new HammingKey("recordLevel", 30, .1, 100, 1000, false, true);

        HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1},true);

        //HammingConfiguration hc = new HammingConfiguration("c:/tmp", "testBer", 30, 105, 0.1, 1200);
        //HammingLSHDB lsh = new HammingLSHStore("c:/tmp", "testBer", hc);
        HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc,true);

        

                String file = "c://voters//test_voters_A.txt";

                FileReader input1 = new FileReader(file);
                BufferedReader bufRead1 = new BufferedReader(input1);
                int lines = 700000; //FileUtil.countLines(file);
                Random r = new Random();
                for (int i = 0; i < lines; i++) {
                    String line1 = bufRead1.readLine();
                    StringTokenizer st1 = new StringTokenizer(line1, ",");
                    String Id = st1.nextToken();
                    String lastName = st1.nextToken();
                    String firstName = st1.nextToken();                    
                    String address = st1.nextToken();
                    String town = "";
                    if (st1.hasMoreTokens())
                         town = st1.nextToken();
                    

                    Record rec = new Record();
                    rec.setId(Id);
                    rec.set("First Name", firstName);
                    rec.set("Last Name", lastName);
                    rec.set("Address", address );
                    rec.set("Town", town);


                    System.out.println(i);
                    lsh.insert(rec);

                }
                
                bufRead1.close();
                input1.close();
            
                lsh.close();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
        
    }
}
