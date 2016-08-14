/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

import gr.eap.LSHDB.BloomFilter;
import gr.eap.LSHDB.HammingConfiguration;
import gr.eap.LSHDB.HammingKey;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Private_Mode {

    
    public void query() {
       try{ 
        String folder = "c:/MAPDB";
        String dbName = "private_voters";
        String engine = "gr.eap.LSHDB.MapDB";

        Key key1 = new HammingKey("recordLevel", 30, .1, 110, 1000, false, true);

        HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1},false);
        System.out.println("private="+hc.isPrivateMode());
        //HammingConfiguration hc = new HammingConfiguration("c:/tmp", "testBer", 30, 105, 0.1, 1200);
        //HammingLSHDB lsh = new HammingLSHStore("c:/tmp", "testBer", hc);
        HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc,false);

       

                String file = "c://voters//blooms_b.txt";

                FileReader input1 = new FileReader(file);
                BufferedReader bufRead1 = new BufferedReader(input1);
                int lines = 100; //FileUtil.countLines(file);
                for (int i = 0; i < lines; i++) {
                    String line1 = bufRead1.readLine();
                    StringTokenizer st1 = new StringTokenizer(line1, ",");
                    String Id = st1.nextToken();
                    String bf = st1.nextToken();
                    
                    QueryRecord rec = new QueryRecord(100);
                    rec.setId(Id);
                    rec.set(Record.PRIVATE_STRUCTURE, BloomFilter.toBitSet(bf));                    
                    rec.set(1.0, true);
                    
                    
                   
                    Result result = lsh.query(rec);
                    result.prepare();
                    if (result!=null){
                        System.out.println(Id);
                     
                        for (int j = 0; j < result.getRecords().size(); j++) {
                            Record record = result.getRecords().get(j);
                            System.out.println("_______"+record.getId());
                        }    
                    }    
                }
                
                bufRead1.close();
                input1.close();
                 lsh.close();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
        
    }
    
    
    
    
    
    public  void insert() {
        try { 
        String folder = "c:/MAPDB";
        String dbName = "private_voters";
        String engine = "gr.eap.LSHDB.MapDB";

        //HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, 30, 140, 0.1, 1200, null);
        //Key key1 = new HammingKey("author", 30, .1, 55, 500, true, true);
        //Key key2 = new HammingKey("title", 30, .1, 55, 500, true, true);
        Key key1 = new HammingKey("recordLevel", 30, .1, 110, 1000, false, true);

        HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1},true);
        hc.setPrivateMode();
        hc.saveConfiguration();
        //HammingConfiguration hc = new HammingConfiguration("c:/tmp", "testBer", 30, 105, 0.1, 1200);
        //HammingLSHDB lsh = new HammingLSHStore("c:/tmp", "testBer", hc);
        HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc,true);

       

                String file = "c://voters//blooms_A.txt";

                FileReader input1 = new FileReader(file);
                BufferedReader bufRead1 = new BufferedReader(input1);
                int lines = 10000; //FileUtil.countLines(file);
                Random r = new Random();
                for (int i = 0; i < lines; i++) {
                    String line1 = bufRead1.readLine();
                    StringTokenizer st1 = new StringTokenizer(line1, ",");
                    String Id = st1.nextToken();
                    String bf = st1.nextToken();
                    
                    Record rec = new Record();
                    rec.setId(Id);
                    rec.set(Record.PRIVATE_STRUCTURE, BloomFilter.toBitSet(bf));                    
                    
                    lsh.insert(rec);
                    System.out.println(i);
                }
                
                bufRead1.close();
                input1.close();
                 lsh.close();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
       
    }
    
     public static void main(String[] args) {
         Private_Mode pm = new Private_Mode();
         pm.query();
     }
}
