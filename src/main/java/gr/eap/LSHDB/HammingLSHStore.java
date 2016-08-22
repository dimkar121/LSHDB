/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.embeddables.BloomFilter;
import gr.eap.LSHDB.util.ListUtil;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.Record;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

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
