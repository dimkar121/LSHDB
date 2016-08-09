/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Record;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.volume.ByteArrayVol;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

/**
 *
 * @author dimkar
 */
public class MapDB implements StoreEngine {

    public String pathToDB;
    public DB db;
    public DB mem;
    HTreeMap map;
    HTreeMap mapDisk;
    Volume memVolume;
    Volume fileVolume;    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    double prop = .1;
    String fileName;

    @Override
    public ArrayList<Record> browse(int rowCount, int pageNo, String key) {
        ArrayList<Record> recs = new ArrayList<Record>();
        /*Pageable p = new Pageable(Collections.list(Collections.enumeration(mapDisk.keySet())));
         p.setPageSize(rowCount);
        p.setPage(pageNo);
        List<String> l = p.getListForPage();
        for (int i = 0; i < l.size(); i++) {
            String keyl = l.get(i);            
            Object data = this.get(keyl);
            Record r = new Record();
            r.setId(keyl);
            r.set(keyl, data);
            recs.add(r);
        }*/
        return recs;
    }

    @Override
    public ArrayList<Record> browseBack(int rowCount, int pageNo, String key) {
        return browse(rowCount, pageNo, key);

    }

    

    public void persist() {
        OutputStream out;        
        System.out.println("persisiting...");
        try {
            out = new FileOutputStream(new File(fileName),true);
            fileVolume.copyTo(out);
            //fileVolume.close();
            //fileVolume = MappedFileVol.FACTORY.makeVolume(fileName, true);    
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MapDB(String folder, String dbName, String entity, boolean massInsertMode) {
        try {
            pathToDB = folder + System.getProperty("file.separator") + dbName;
            File theDir = new File(pathToDB);
            if (!theDir.exists()) {
                theDir.mkdir();
            }
            fileName = pathToDB + System.getProperty("file.separator") + entity;
            //System.out.println(wholeFileName);

            //outputStream = new FileOutputStream(wholeFileName+"LL");
            //File f = File.createTempFile(wholeFileName, null);
            //memVolume = MappedFileVol.FACTORY.makeVolume(wholeFileName, false);
            //memVolume.fileLoad();
            //System.out.println(memVolume.getFile().getAbsolutePath());
            //load(wholeFileName);            
            //boolean contentAlreadyExists =  new File(wholeFileName).exists();
            //db = DBMaker.volumeDB(memVolume, contentAlreadyExists).closeOnJvmShutdown().make();
            //db = DBMaker.fileDB(wholeFileName).checksumHeaderBypass().closeOnJvmShutdown().make();
            if (massInsertMode) {
                db = DBMaker.fileDB(fileName).fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable().checksumHeaderBypass().closeOnJvmShutdown().make();
                mem = DBMaker.memoryDB().make();

            } else {
                 db = DBMaker.fileDB(fileName).fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable().checksumHeaderBypass().closeOnJvmShutdown().make();
                 mem = DBMaker.memoryDB().make();
                //MappedFileVol fileVolume = (MappedFileVol) MappedFileVol.FACTORY.makeVolume(fileName,true);
                //fileVolume.fileLoad();
                
                //boolean contentAlreadyExists = new File(fileName).exists();
                //Volume fileVolume = MappedFileVol.FACTORY.makeVolume(fileName,contentAlreadyExists);
                //memVolume = new ByteArrayVol();                
                //fileVolume.copyTo(memVolume);
                
                //db = DBMaker.volumeDB(memVolume, contentAlreadyExists).checksumHeaderBypass().make();
                //mapDisk = db.hashMap(entity).createOrOpen();
                //db = DBMaker.fileDB(fileName).fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable().checksumHeaderBypass().closeOnJvmShutdown().make();
                //mem = DBMaker.memoryDB().make();
            }

             mapDisk = db.hashMap(entity).createOrOpen();
             map = mem.hashMap(entity).expireAfterGet(1, TimeUnit.SECONDS).expireExecutor(Executors.newScheduledThreadPool(2)).create();
             
            /* Set set = mapDisk.getKeys();
            int size = set.size();
            int limit =(int) Math.round(size*prop);
            Iterator it = set.iterator();
            for (int i=0;i<limit; i++){
                 Object k =  it.next();
                 Object o = mapDisk.get(k);
                 map.put(k,o);
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        //OutputStream out = new ByteArrayOutputStream();
        //memVolume.copyTo(outputStream);
        // outputStream.close();

        //memVolume.close();
        //db.close();        
        //save();
        map.close();
        mapDisk.close();
    }

    public synchronized Object get(String key) {
        try {
            if (map.containsKey(key)) {
                Object o = map.get(key);
                return o;
            } else if (mapDisk.containsKey(key)) {
                Object o = mapDisk.get(key);
                //map.put(key,o);                    
                return o;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public synchronized boolean contains(String key) {
         if (map.containsKey(key)) {
           return true;
        }
        if (mapDisk.containsKey(key)) {
            if (! map.containsKey(key))
              map.put(key, mapDisk.get(key));
            return true;
        }
        return false;
    }

    public synchronized void set(String key, Object data) {
       mapDisk.put(key, data);
       // map.put(key, data);
    }

    public synchronized void persistentSet(String key, Object data) {
        mapDisk.put(key, data);
    }

    public synchronized boolean persistentContains(String key) {

        if (mapDisk.containsKey(key)) {
            return true;
        }
        return false;
    }

    public long count() {
        return mapDisk.size();
    }
}
