/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Record;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Cursor;

import com.sleepycat.je.DatabaseException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/**
 *
 * @author dimkar
 */
public class BerkeleyDB implements StoreEngine {

    Database db;
    Environment dbEnv;

    public BerkeleyDB(String folder, String dbName, String entity,boolean massInsertMode) {
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
            db = dbEnv.openDatabase(null, dbName + "-" + entity, dbConf);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public long count(){
       try{ 
        return db.count();
       }catch(DatabaseException ex){
           ex.printStackTrace();
       } 
      return 0; 
    }

    

    public void set(String key, Object data) {
        try {
            DatabaseEntry dbKey = new DatabaseEntry(key.getBytes("UTF-8"));
            //System.out.println(serialize(data));
            DatabaseEntry dbData = new DatabaseEntry(DataStore.serialize(data));
            if ((db.put(null, dbKey, dbData)
                    == OperationStatus.SUCCESS)) {

            } else {
                System.out.println("Failed writing for " + key);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // testDB.get(null, key, data, null);

    }

    public void close() {
        try {
            db.close();
            dbEnv.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void persist(){
        
    }
    
   

    public Object get(String key) {
        try {
            DatabaseEntry dbKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry dbData = new DatabaseEntry();

            //StringBinding.stringToEntry(key, dbKey);
            if ((db.get(null, dbKey, dbData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS)) {
                return DataStore.deserialize(dbData.getData());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public ArrayList<Record> browse(int rowCount, int pageNo, String key) {
        ArrayList<Record> recs = new ArrayList<Record>();
        Cursor cursor = null;
        try {
            int a = 0;
            cursor = db.openCursor(null, null);
            //cursor.
            // Cursors need a pair of DatabaseEntry objects to operate. These hold
            // the key and data found at any given position in the database.
            DatabaseEntry foundKey = new DatabaseEntry(key.getBytes());
            DatabaseEntry foundData = new DatabaseEntry();

            // To iterate, just call getNext() until the last database record has been 
            // read. All cursor operations return an OperationStatus, so just read 
            // until we no longer see OperationStatus.SUCCESS
            if (! key.equals(""))
                 cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT);
          
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS) {
                // getData() on the DatabaseEntry objects returns the byte array
                // held by that object. We use this to get a String value. If the
                // DatabaseEntry held a byte array representation of some other data
                // type (such as a complex object) then this operation would look 
                // considerably different.
                String keyString = new String(foundKey.getData());
                Object data = DataStore.deserialize(foundData.getData());
                Record r = new Record();
                r.setId(keyString);
                r.set(keyString, data);
                //System.out.println(keyString+" "+data);
                recs.add(r);
                a++;
                if (a == rowCount) {
                    cursor.close();
                    return recs;
                }
                
            }
            cursor.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
        return recs;
    }

    @Override
    public ArrayList<Record> browseBack(int rowCount, int pageNo,String key) {
        ArrayList<Record> recs = new ArrayList<Record>();
        Cursor cursor = null;
        try {
            int a = 0;
            cursor = db.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry(key.getBytes());
            DatabaseEntry foundData = new DatabaseEntry();
            if (! key.equals(""))
                 cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT);
          
            while (cursor.getPrev(foundKey, foundData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS) {
                String keyString = new String(foundKey.getData());
                Object data = DataStore.deserialize(foundData.getData());
                Record r = new Record();
                r.setId(keyString);
                r.set(keyString, data);
                //System.out.println(keyString+" "+data);
                recs.add(r);
                a++;
                if (a == rowCount) {
                    cursor.close();
                    Collections.reverse(recs);           
                    return recs;
                }
                
            }
            Collections.reverse(recs);
            cursor.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
        return recs;
    }
    
    
   
    
    
    
    public static void main(String[] args) {
        try {
           // StoreEngine db = BuildDB.build("c:/tmp", "BerkeleyDB", "Entity");
            // key
            //db.set("key1" , "lalakis");
            //System.out.println(db.get("key1"));

            //db.close();
            Stack q=new Stack();
            q.add("third");
            q.add("second");
            q.add("first");
            ArrayList<String> arr=new ArrayList<String>(q);
            System.out.println(arr.get(0));
            System.out.println(arr.get(1));
            System.out.println(arr.get(2));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
