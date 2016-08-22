/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.embeddables.BloomFilter;
import gr.eap.LSHDB.util.Config;
import java.util.BitSet;
import org.w3c.dom.Element;

/**
 *
 * @author dimkar
 */
public class HammingLSHStore extends DataStore {

    HammingConfiguration hConf;

    public HammingLSHStore(String folder, String dbName, String dbEngine, Configuration hc, boolean massInsertMode) throws StoreInitException {
        this.folder = folder;
        this.dbName = dbName;
        this.massInsertMode = massInsertMode;
        if (hc == null) {
            hConf = new HammingConfiguration(folder, dbName, dbEngine, massInsertMode);
        } else {
            hConf = (HammingConfiguration) hc;
        }        
        init(dbEngine, massInsertMode);

    }

    /*
    * Opens a HammingLSH store
    * found in specified @target.
    * @throws StoreInitExcoetion
    */
    public static HammingLSHStore open(String storeName) throws StoreInitException{
        Config conf = new Config(Config.CONFIG_FILE);
        Element el = conf.get(Config.CONFIG_STORE, storeName);
        if (el!=null){
            String configuration = el.getElementsByTagName(Config.CONFIG_CONFIGURATION).item(0).getNodeValue();
            String engine = el.getElementsByTagName(Config.CONFIG_NOSQL_ENGINE).item(0).getNodeValue();
            String target = el.getElementsByTagName(Config.CONFIG_TARGET).item(0).getNodeValue();
            log.info(engine+" "+target+" "+configuration);
            //return new HammingLSHStore(target,storeName,engine,null,false);
            
        }
        throw new StoreInitException("store "+storeName+" not initialized. Check config.xml ");
    }
    
    public HammingLSHStore(String folder, String dbName, String dbEngine) throws StoreInitException {
        this.folder = folder;
        this.dbName = dbName;
        hConf = new HammingConfiguration(folder, dbName, dbEngine, massInsertMode);
        init(dbEngine, massInsertMode);
        hConf.saveConfiguration();
    }

    public Configuration getConfiguration() {
        return this.hConf;
    }

    
    
    
    public boolean distance(Embeddable struct1, Embeddable struct2, Key key) {
        BloomFilter bf1 = (BloomFilter) struct1;
        BloomFilter bf2 = (BloomFilter) struct2;
        int t = ((HammingKey) key).t;
        if (key.thresholdRatio > 0.0) {
            t = (int) Math.round(t * key.thresholdRatio);
        }
        BitSet bs1 = bf1.getBitSet();
        BitSet bs2 = bf2.getBitSet();
        int d = distance(bs1, bs2);
        return (d <= t);            
    }

    @Override
    public String buildHashKey(int j, Embeddable emb, String keyFieldName) {
        BloomFilter bf = (BloomFilter) emb;
        BitSet bs = bf.getBitSet();
        String hashKey = "";
        Key key = hConf.getKey(keyFieldName);

        for (int k = 0; k < key.k; k++) {
            int position = ((HammingKey) key).samples[j][k];
            if (bs.get(position)) {
                hashKey = hashKey + "1";
            } else {
                hashKey = hashKey + "0";
            }

        }
        return "L" + j + "_" + hashKey;
    }

    public BitSet toBitSet(String bf) {
        BitSet bs = new BitSet(bf.length());
        for (int i = 0; i < bf.length(); i++) {
            if (bf.charAt(i) == '1') {
                bs.set(i);
            }
        }
        return bs;
    }

    public int distance(BitSet a, BitSet b) {
        int d = 0;
        BitSet c = (BitSet) a.clone();
        c.xor(b);
        d = c.cardinality();
        return d;
    }

    
    
      
    
     
    
    
    public void save() {

    }

}
