/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author dimkar
 */
public abstract class Configuration implements Serializable {

    public static final long serialVersionUID = 100L;
    
    String folder;
    String dbName;
    StoreEngine db;
    String[] keyFieldNames;
    boolean isKeyed = false;
    boolean isPrivate = false;

    public static String RECORD_LEVEL = "recordLevel";
    public static String PRIVATE_MODE = "privateLevel";
    public static String KEY_NAMES = "keyFieldNames";
    public static String KEY_MODE = "isKeyed";

    HashMap<String, Key> keys = new HashMap<String, Key>();

    public String[] getKeyFieldNames() {
        return keyFieldNames;
    }

    public boolean isKeyed() {
        return isKeyed;
    }

    public boolean isPrivateMode() {
        return (isPrivate == true);
    }

    public void setPrivateMode() {
        isPrivate = true;
        db.set(PRIVATE_MODE, true);
    }

    public Key getKey(String key) {
        if (keys.containsKey(key)) {
            return keys.get(key);
        }
        return null;
    }

    public void saveConfiguration() {
        close();
    }
    public void close(){
        db.close();
    }

    public Configuration(String folder, String dbName, String dbEngine, boolean massInsertMode) throws StoreInitException {
        try{
            this.folder = folder;
            this.dbName = dbName;
            db = DataStoreFactory.build(folder, dbName, "conf", dbEngine, massInsertMode);

            if (db.contains(Configuration.KEY_NAMES)) {
                this.keyFieldNames = (String[]) db.get(Configuration.KEY_NAMES);
            }

            if (db.contains(Configuration.KEY_MODE)) {
                this.isKeyed = (boolean) db.get(Configuration.KEY_MODE);
            }

            if (keyFieldNames != null) {
                for (int i = 0; i < this.keyFieldNames.length; i++) {
                    String keyFieldName = this.keyFieldNames[i];
                    keys.put(keyFieldName, (Key) db.get("conf_" + keyFieldName));
                }

            }
        } catch (ClassNotFoundException ex) {
            throw new StoreInitException("Decalred class " + dbEngine + " not found.");
        } catch (NoSuchMethodException ex) {
            throw new StoreInitException("The particular constructor cannot be found in the decalred class " + dbEngine + ".");
        }
    }

    public Configuration(String folder, String dbName, String dbEngine, Key[] keysList, boolean massInsertMode) throws StoreInitException {
        try {
            this.folder = folder;
            this.dbName = dbName;
            db = DataStoreFactory.build(folder, dbName, "conf", dbEngine, massInsertMode);

            if (db.contains(Configuration.KEY_NAMES)) {
                this.keyFieldNames = (String[]) db.get(Configuration.KEY_NAMES);
                for (int i = 0; i < this.keyFieldNames.length; i++) {
                    String keyFieldName = this.keyFieldNames[i];
                    this.keys.put(keyFieldName, (Key) db.get("conf_" + keyFieldName));
                }
                if (db.contains(Configuration.KEY_MODE)) {
                    this.isKeyed = (boolean) db.get(Configuration.KEY_MODE);
                }
                if (db.contains(Configuration.PRIVATE_MODE)) {
                    this.setPrivateMode();
                }

            } else {
                this.keyFieldNames = new String[keysList.length];
                for (int i = 0; i < keysList.length; i++) {
                    this.keyFieldNames[i] = keysList[i].getKeyFieldName();
                    keys.put(keyFieldNames[i], keysList[i]);
                    db.set("conf_" + this.keyFieldNames[i], keysList[i]);
                    if (this.keyFieldNames[0].equals(Configuration.RECORD_LEVEL)) {
                        this.isKeyed = false;
                    } else {
                        this.isKeyed = true;
                    }
                }
                db.set(Configuration.KEY_MODE, this.isKeyed);
                db.set(Configuration.KEY_NAMES, this.keyFieldNames);

            }
        } catch (ClassNotFoundException ex) {
            throw new StoreInitException("Decalred class " + dbEngine + " not found.");
        } catch (NoSuchMethodException ex) {
            throw new StoreInitException("The particular constructor cannot be found in the decalred class " + dbEngine + ".");
        }
    }
    
}
