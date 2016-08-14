/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.ListUtil;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.Record;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author dimkar
 */
public class HammingLSHStore extends DataStore {

    HammingConfiguration hConf;

    public HammingLSHStore(String folder, String dbName, String dbEngine, Configuration hc,boolean massInsertMode) throws StoreInitException  {
        this.folder = folder;
        this.dbName = dbName;
        this.massInsertMode = massInsertMode;
        if (hc == null) {
            hConf = new HammingConfiguration(folder, dbName, dbEngine,massInsertMode  );
        } else {
            hConf = (HammingConfiguration) hc;
        }
        init(dbEngine,massInsertMode);

    }
    
    public HammingLSHStore(String folder, String dbName, String dbEngine) throws StoreInitException {
        this.folder = folder;
        this.dbName = dbName;
        hConf = new HammingConfiguration(folder, dbName, dbEngine,massInsertMode);        
        init(dbEngine,massInsertMode);
        hConf.saveConfiguration();
    }
    
    
    
    

    public Configuration getConfiguration() {
        return this.hConf;
    }

    public String buildKey(int j, BitSet bs, String keyFieldName) {
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

    

    public void insert(Record rec) {
        if (hConf.isPrivateMode()){
            BitSet bs = (BitSet) rec.get(Record.PRIVATE_STRUCTURE);
            data.set(rec.getId(), bs);      
            hash(rec.getId(), bs, Configuration.RECORD_LEVEL);
            return;
        } 
         
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        HashMap<String, BloomFilter[]> bfMap = toBloomFilter(rec);

        if (isKeyed) {
            for (int i = 0; i < keyFieldNames.length; i++) {
                String keyFieldName = keyFieldNames[i];
                BloomFilter[] bfs = bfMap.get(keyFieldName);
                //BitSet[] bss = new BitSet[bfs.length];
                for (int j = 0; j < bfs.length; j++) {
                    BloomFilter bf = bfs[j];
                    BitSet bs = bf.getBitSet();
                    hash(rec.getId() + Key.KEYFIELD + j, bf.getBitSet(), keyFieldName);
                    this.getDataMap(keyFieldName).set(rec.getId() + Key.KEYFIELD + j, bs);
                }

            }
        } else {
            data.set(rec.getId(), bfMap.get(Configuration.RECORD_LEVEL)[0].getBitSet());
            hash(rec.getId(), bfMap.get(Configuration.RECORD_LEVEL)[0].getBitSet(), Configuration.RECORD_LEVEL);
        }

        records.set(rec.getId(), rec);
    }

    public void hash(String id, Object dataPoint, String keyFieldName) {

        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        StoreEngine hashKeys = keys;
        if (isKeyed) {
            hashKeys = this.getKeyMap(keyFieldName);
        }
        BitSet bs = (BitSet) dataPoint;
        Key key = hConf.getKey(keyFieldName);

        for (int j = 0; j < key.L; j++) {
            String hashKey = buildKey(j, bs, keyFieldName);
            ArrayList<String> arr = new ArrayList<String>();

            if (hashKeys.contains(hashKey)) {
                arr = (ArrayList) hashKeys.get(hashKey);
                arr.add(id);

            } else {
                arr.add(id);
            }
            hashKeys.set(hashKey, arr);
        }
    }

    public void queryBitSet(BitSet queryBs, QueryRecord queryRec, String keyFieldName, Result result) {
        int rowCount = queryRec.getMaxQueryRows();
        boolean performComparisons = queryRec.performComparisons(keyFieldName);
        double userPercentageThreshold = queryRec.getUserPercentageThreshold(keyFieldName);
        StoreEngine keys = this.getKeyMap(keyFieldName);
        StoreEngine data = this.getDataMap(keyFieldName);
        Key key = hConf.getKey(keyFieldName);
        boolean isPrivateMode = hConf.isPrivateMode();
        int t = ((HammingKey) key).t;
        if (userPercentageThreshold > 0.0) {
            t = (int) Math.round(((HammingKey) key).t * userPercentageThreshold);
        }
        int a = 0;
        int pairs = 0;
        for (int j = 0; j < key.L; j++) {
           
            if (a >= rowCount) {
                //System.out.println("Performed totally "+pairs+" comparisons. Running on "+j);  
                break;
            }
            String hashKey = buildKey(j, queryBs, keyFieldName);
            if (keys.contains(hashKey)) {
                ArrayList arr = (ArrayList) keys.get(hashKey);
                for (int i = 0; i < arr.size(); i++) {
                    String id = (String) arr.get(i);

                    CharSequence cSeq = Key.KEYFIELD;
                    String idRec = id;
                    if (idRec.contains(cSeq)) {
                        idRec = id.split(Key.KEYFIELD)[0];
                    }
                    Record dataRec = null;
                    if (!hConf.isPrivateMode())
                      dataRec= (Record) records.get(idRec);   // which id and which record shoudl strip the "_keyField_" part , if any
                    else {
                        dataRec= new Record();
                        dataRec.setId(idRec);
                    }
                        
                    if ((performComparisons) && (!result.getMap(keyFieldName).containsKey(id))) {

                        BitSet bs = (BitSet) data.get(id); //issue: many bitSets accumulated

                       
                        pairs++;
                        int d = distance(queryBs, bs);
                       
                        
                        if (d <= t) {
                            if (result.add(keyFieldName, dataRec)) {                                
                                a++;
                            }

                        }

                       
                    } else {
                        pairs++;
                        result.add(keyFieldName, dataRec);
                    }

                }
            }
        }
   
    }

    @Override
    public Result query(QueryRecord queryRecord) throws NoKeyedFieldsException{
        StoreEngine hashKeys = keys;
        StoreEngine dataKeys = data;
        HashMap<String, BloomFilter[]> bfMap = null; 
        if (! hConf.isPrivateMode())
           bfMap = toBloomFilter(queryRecord);
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        //    BitSet queryBs = bf[0].getBitSet();
        Result result = new Result(queryRecord);
        HashSet set = new HashSet<Record>();
        ArrayList<Record> finalRecordList = new ArrayList<Record>();
        ArrayList<String> fieldNames = queryRecord.getFieldNames();
        
        if ((fieldNames.size()==0) && (hConf.isKeyed))
               throw new NoKeyedFieldsException(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG);
        if(ListUtil.intersection(fieldNames, Arrays.asList(keyFieldNames)).size()==0  && (hConf.isKeyed)){
               throw new NoKeyedFieldsException(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG);                
        }        
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            if (keyFieldNames != null) {
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    if (keyFieldName.equals(fieldName)) {
                        BloomFilter[] bfs = bfMap.get(fieldName);
                        for (int k = 0; k < bfs.length; k++) {
                            BitSet queryBs = bfs[k].getBitSet();
                            queryBitSet(queryBs, queryRecord, keyFieldName, result);

                        }

           
                    }
                }
            }

        }

        if (!isKeyed)  {
            BitSet queryBs = null;
            if (hConf.isPrivateMode())
                queryBs =(BitSet) queryRecord.get(Record.PRIVATE_STRUCTURE);
            else 
                queryBs = bfMap.get(Configuration.RECORD_LEVEL)[0].getBitSet();
            queryBitSet(queryBs, queryRecord, Configuration.RECORD_LEVEL, result);
        }
        
         result = queryRemoteNodes(queryRecord,result);


        return result;
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

    public ArrayList<Result> browse(int rowCount) {
        ArrayList<Result> results = new ArrayList<Result>();

        return results;
    }
    
    
    public void save(){
        
    }

    
}
