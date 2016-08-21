/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.Property;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.Record;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author dimkar
 */
public class MinHashLSHStore extends DataStore {

    MinHashConfiguration hConf;

    public MinHashLSHStore(String folder, String dbName, String dbEngine, Configuration hc,boolean massInsertMode) throws StoreInitException {
        this.folder = folder;
        this.dbName = dbName;
        this.massInsertMode = massInsertMode;
        if (hc == null) {
            hConf = new MinHashConfiguration(folder, dbName, dbEngine,massInsertMode);
        } else {
            hConf =(MinHashConfiguration) hc;
        }
        init(dbEngine,massInsertMode);

    }

    public Configuration getConfiguration() {
        return this.hConf;
    }

    
    
    @Override
    public String buildHashKey(int j, EmbeddingStructure emb, String keyFieldName) {       
        BloomFilter bf = (BloomFilter) emb;        
        BitSet bs = bf.getBitSet();
        Key key = hConf.getKey(keyFieldName);
        int permNo = ((MinHashKey)  key).permutationsNo;
        int[][][] permutations = ((MinHashKey)  key).permutations;
        int L =  ((MinHashKey)  key).L;
        int size  = ((MinHashKey)  key).size;
        int k  = ((MinHashKey)  key).k;        
        
        int min[] = new int[permNo];
        for (int i = 0; i < permNo; i++) {
            min[i] = Integer.MAX_VALUE;
        }
        StringBuffer hashKey=new StringBuffer("L" + j + "_");
        for (int i = 0; i < k; i++) {
            for (int m = 0; m < size; m++) {
                if ( bs.get(permutations[j][i][m]) ) { //permutations in this context
                    min[i] = permutations[j][i][m];
                    //min[i]=j;
                   String ss = String.format("%0" + 4 + "d", min[i]);                     
                   hashKey.append(ss);
                   break;
                }
            }

        }        
       return hashKey.toString(); 
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

    @Override
    public HashMap<String, BloomFilter[]> buildEmbeddingStructureMap(Record rec){
            return toBloomFilter(rec);
    }
    
    
    public boolean distance(EmbeddingStructure struct1, EmbeddingStructure struct2, Key key) {
        BloomFilter bf1 = (BloomFilter) struct1;
        BloomFilter bf2 = (BloomFilter) struct2;
        double t = ((MinHashKey) key).t;
        if (key.thresholdRatio > 0.0) {
            t = (int) Math.round(t * key.thresholdRatio);
        }
        BitSet bs1 = bf1.getBitSet();
        BitSet bs2 = bf2.getBitSet();
        
        double d = distance(bs1, bs2);
        return (d <= t);            
    }
    
    
    public double distance(BitSet x, BitSet y) {
        int a = 0;
        int b = 0;
        int c = 0;

        for (int i = 0; i < x.length(); i++) {
            boolean xc = x.get(i);
            boolean yc = y.get(i);

            if ((xc) && (yc)) {
                a++;
            }
            if ((! xc) && (yc)) {
                b++;
            }
            if ((xc) && (! yc )) {
                c++;
            }
        }

        return 1 - (a * 1.0) / ((a + b + c) * 1.0);

    } 

    
    
    

   
}
