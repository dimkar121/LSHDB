/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.Record;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import gr.eap.LSHDB.client.Client;
import gr.eap.LSHDB.util.ListUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 *
 * @author dimkar
 */
public abstract class DataStore {

    String folder;
    String dbName;
    String dbEngine;
    public String pathToDB;
    public StoreEngine data;
    public StoreEngine keys;
    public StoreEngine records;
    HashMap<String, StoreEngine> keyMap = new HashMap<String, StoreEngine>();
    HashMap<String, StoreEngine> dataMap = new HashMap<String, StoreEngine>();
    ArrayList<Node> nodes = new ArrayList<Node>();
    boolean queryRemoteNodes = false;
    boolean massInsertMode = false;

    public void setMassInsertMode(boolean status) {
        massInsertMode = status;
    }

    public boolean getMassInsertMode() {
        return massInsertMode;
    }

    public void setQueryMode(boolean status) {
        queryRemoteNodes = status;
    }

    public boolean getQueryMode() {
        return queryRemoteNodes;
    }

    public Node getNode(String alias) {
        for (int i = 0; i < this.getNodes().size(); i++) {
            Node node = this.getNodes().get(i);
            if (node.alias.equals(alias)) {
                return node;
            }
        }
        return null;
    }

    public ArrayList<Node> getNodes() {
        return this.nodes;
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public StoreEngine getKeyMap(String fieldName) {
        fieldName = fieldName.replaceAll(" ", "");
        return keyMap.get(fieldName);
    }

    public void setKeyMap(String fieldName, boolean massInsertMode) throws NoSuchMethodException, ClassNotFoundException {
        fieldName = fieldName.replaceAll(" ", "");
        keyMap.put(fieldName, DataStoreFactory.build(folder, dbName, "keys_" + fieldName, dbEngine, massInsertMode));
    }

    public StoreEngine getDataMap(String fieldName) {
        fieldName = fieldName.replaceAll(" ", "");
        return dataMap.get(fieldName);
    }

    public void setDataMap(String fieldName, boolean massInsertMode) throws NoSuchMethodException, ClassNotFoundException {
        fieldName = fieldName.replaceAll(" ", "");
        dataMap.put(fieldName, DataStoreFactory.build(folder, dbName, "data_" + fieldName, dbEngine, massInsertMode));
    }

    public void init(String dbEngine, boolean massInsertMode) throws StoreInitException {
        try {
            this.dbEngine = dbEngine;
            pathToDB = folder + System.getProperty("file.separator") + dbName;
            records = DataStoreFactory.build(folder, dbName, "records", dbEngine, massInsertMode);
            if ((this.getConfiguration() != null) && (this.getConfiguration().isKeyed())) {
                String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    setKeyMap(keyFieldName, massInsertMode);
                    setDataMap(keyFieldName, massInsertMode);
                }
            } else {
                keys = DataStoreFactory.build(folder, dbName, "keys", dbEngine, massInsertMode);
                data = DataStoreFactory.build(folder, dbName, "data", dbEngine, massInsertMode);
                keyMap.put("recordLevel", keys);
                dataMap.put("recordLevel", data);

            }
        } catch (ClassNotFoundException ex) {
            throw new StoreInitException("Decalred class " + dbEngine + " not found.");
        } catch (NoSuchMethodException ex) {
            throw new StoreInitException("The particular constructor cannot be found in the decalred class " + dbEngine + ".");
        }
    }

    public void persist() {
        records.persist();
        if (this.getConfiguration().isKeyed()) {
            String[] keyFieldNames = this.getConfiguration().keyFieldNames;
            for (int j = 0; j < keyFieldNames.length; j++) {
                String indexFieldName = keyFieldNames[j];
                StoreEngine dataFactory = getKeyMap(indexFieldName);
                dataFactory.persist();
                dataFactory = getDataMap(indexFieldName);
                dataFactory.persist();
            }
        } else {
            data.persist();
            keys.persist();
        }
    }

    public void close() {

        records.close();
        if (this.getConfiguration().isKeyed()) {
            String[] keyFieldNames = this.getConfiguration().keyFieldNames;
            for (int j = 0; j < keyFieldNames.length; j++) {
                String indexFieldName = keyFieldNames[j];
                StoreEngine dataFactory = getKeyMap(indexFieldName);
                dataFactory.close();
                dataFactory = getDataMap(indexFieldName);
                dataFactory.close();
            }
        } else {
            data.close();
            keys.close();
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getDbEngine() {
        return this.dbEngine;
    }

    public Result forkQuery(QueryRecord queryRecord) {
        Result result = new Result(queryRecord); 
        if (this.getNodes().size() == 0) {
            return result;
        }
        // should implment get Active Nodes
        ExecutorService executorService = Executors.newFixedThreadPool(this.getNodes().size());
        List<Callable<Result>> callables = new ArrayList<Callable<Result>>();

        final QueryRecord q = queryRecord;
        for (int i = 0; i < this.getNodes().size(); i++) {
            final Node node = this.getNodes().get(i);
            if (node.isEnabled()) {
                callables.add(new Callable<Result>() {
                    public Result call()  {
                        Result r = null;        
                        if ((!node.isLocal()) && (q.isClientQuery())) {
                            Client client = new Client(node.url, node.port);
                            try {
                                QueryRecord newQuery = (QueryRecord) q.clone();
                                newQuery.setServerQuery();                                
                                r = client.queryServer(newQuery);
                                if (r == null) {
                                    r = new Result(newQuery);
                                    r.setStatus(Result.NULL_RESULT_RETURNED);
                                }                                
                                r.setRemote();
                                r.setOrigin(node.alias);                                
                            } catch (CloneNotSupportedException | NodeCommunicationException ex) {
                                if (r==null)                                    
                                    r=new Result(q);
                                r.setRemote();
                                r.setOrigin(node.alias);
                                r.setStatus(Result.NO_CONNECT);
                            }

                        } else if (node.isLocal()) {
                           try{ 
                              r = query(q);
                              r.setStatus(Result.STATUS_OK);
                              r.prepare();
                              r.setOrigin(node.alias);
                           }catch(NoKeyedFieldsException ex){
                                if (r!=null)                                    
                                    r=new Result(q);
                               r.setOrigin(node.alias);
                               r.setStatus(Result.NO_KEYED_FIELDS_SPECIFIED);
                           }   
                        }
                        return r;
                    }
                });
            }
        }

        Result partialResults = null;
        try {
            List<Future<Result>> futures = executorService.invokeAll(callables);

            for (Future<Result> future : futures) {
                    
                if (future != null) {  //partialResults should not come null
                    
                    partialResults = future.get();
                    if (partialResults != null) {
                        result.getRecords().addAll(partialResults.getRecords());
                        result.setStatus(partialResults.getOrigin() , partialResults.getStatus());
                    }
                }

            }
        } catch (ExecutionException ex) {
            if (ex.getCause() != null) {
                System.out.println(ex.getCause().getMessage());
                if (ex.getCause() instanceof Error) {
                    System.out.println("Fatal error occurred on " + partialResults.getOrigin());
                    Node node = getNode(partialResults.getOrigin());
                    if (node != null) {
                        node.disable();
                    }
                }

            }
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }

        executorService.shutdown();
        return result;
    }

    

    public int getThreadsNo() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.getThreadCount();
    }

    public void forkHashTables(EmbeddingStructure struct1, QueryRecord queryRec, String keyFieldName, Result result) {
        final Configuration conf = this.getConfiguration();
        final int maxQueryRows = queryRec.getMaxQueryRows();
        final boolean performComparisons = queryRec.performComparisons(keyFieldName);
        final double userPercentageThreshold = queryRec.getUserPercentageThreshold(keyFieldName);
        final StoreEngine keys = this.getKeyMap(keyFieldName);
        final StoreEngine data = this.getDataMap(keyFieldName);
        final Key key = conf.getKey(keyFieldName);
        boolean isPrivateMode = conf.isPrivateMode();

        ExecutorService executorService = Executors.newFixedThreadPool(key.L);
        //System.out.println("Active threads=" + getThreadsNo());
        List<Callable<Result>> callables = new ArrayList<Callable<Result>>();

        final Result result1 = result;
        final String keyFieldName1 = keyFieldName;
        final EmbeddingStructure struct11 = struct1;

        for (int j = 0; j < key.L; j++) {
            final int hashTableNo = j;
            callables.add(new Callable<Result>() {
                public Result call() throws StoreInitException, NoKeyedFieldsException {
                    String hashKey = buildHashKey(hashTableNo, struct11, keyFieldName1);
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
                            if (!conf.isPrivateMode()) {
                                dataRec = (Record) records.get(idRec);   // which id and which record shoudl strip the "_keyField_" part , if any
                            } else {
                                dataRec = new Record();
                                dataRec.setId(idRec);
                            }
                            result1.incPairsNo();
                            if ((performComparisons) && (!result1.getMap(keyFieldName1).containsKey(id))) {
                                EmbeddingStructure struct2 = (EmbeddingStructure) data.get(id);
                                key.thresholdRatio = userPercentageThreshold;
                                if (distance(struct11, struct2, key)) {
                                    result1.add(keyFieldName1, dataRec);
                                    int matchesNo = result1.getDataRecordsSize(keyFieldName1);
                                    if (matchesNo >= maxQueryRows) {
                                        return result1;
                                    }
                                } else {
                                }

                            } else {
                                result1.add(keyFieldName1, dataRec);
                            }

                        }
                    }
                    return result1;
                }
            });
        }

        try {
            List<Future<Result>> futures = executorService.invokeAll(callables);
            int k = 0;
            for (Future<Result> future : futures) {

                if (future != null) {
                    Result partialResults = future.get();
                    //if (k == 0) 
                    //  System.out.println("pairsNo=" + partialResults.getPairsNo());

                    k++;
                    if (partialResults != null) {
                        result.getRecords().addAll(partialResults.getRecords());
                    }
                }
            }
        } catch (ExecutionException ex) {
            System.out.println("......................Cannot create threads");
            System.out.println(ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("......................Cannot create threads");
            System.out.println(ex.getMessage());
        }
        executorService.shutdown();

    }

    public void setHashKeys(String id, EmbeddingStructure emb, String keyFieldName) {
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        StoreEngine hashKeys = keys;
        if (isKeyed) {
            hashKeys = this.getKeyMap(keyFieldName);
        }

        Key key = this.getConfiguration().getKey(keyFieldName);

        for (int j = 0; j < key.L; j++) {
            String hashKey = buildHashKey(j, emb, keyFieldName);
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

    public void insert(Record rec) {
        if (this.getConfiguration().isPrivateMode()) {
            EmbeddingStructure emb = (EmbeddingStructure) rec.get(Record.PRIVATE_STRUCTURE);
            data.set(rec.getId(), emb);
            setHashKeys(rec.getId(), emb, Configuration.RECORD_LEVEL);
            return;
        }

        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        HashMap<String, ? extends EmbeddingStructure[]> embMap = createKeyFieldEmbeddingStructureMap(rec);

        if (isKeyed) {
            for (int i = 0; i < keyFieldNames.length; i++) {
                String keyFieldName = keyFieldNames[i];
                EmbeddingStructure[] embs = embMap.get(keyFieldName);
                for (int j = 0; j < embs.length; j++) {
                    EmbeddingStructure emb = embs[j];
                    setHashKeys(rec.getId() + Key.KEYFIELD + j, emb, keyFieldName);
                    this.getDataMap(keyFieldName).set(rec.getId() + Key.KEYFIELD + j, emb);
                }

            }
        } else {
            data.set(rec.getId(), ((EmbeddingStructure[]) embMap.get(Configuration.RECORD_LEVEL))[0]);
            setHashKeys(rec.getId(), ((EmbeddingStructure[]) embMap.get(Configuration.RECORD_LEVEL))[0], Configuration.RECORD_LEVEL);
        }

        records.set(rec.getId(), rec);
    }

    public Result query(QueryRecord queryRecord) throws NoKeyedFieldsException {
        Result result = new Result(queryRecord);

        Configuration conf = this.getConfiguration();
        StoreEngine hashKeys = keys;
        StoreEngine dataKeys = data;
        HashMap<String, ? extends EmbeddingStructure[]> embMap = null;
        if (!conf.isPrivateMode()) {
            embMap = createKeyFieldEmbeddingStructureMap(queryRecord);
        }
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        ArrayList<String> fieldNames = queryRecord.getFieldNames();

        if ((fieldNames.isEmpty()) && (conf.isKeyed)) {
            throw new NoKeyedFieldsException(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG);
        }
        if (ListUtil.intersection(fieldNames, Arrays.asList(keyFieldNames)).isEmpty() && (conf.isKeyed)) {
            throw new NoKeyedFieldsException(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG);
        }

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            if (keyFieldNames != null) {
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    if (keyFieldName.equals(fieldName)) {
                        EmbeddingStructure[] structArr = embMap.get(fieldName);
                        for (int k = 0; k < structArr.length; k++) {
                            forkHashTables(structArr[k], queryRecord, keyFieldName, result);

                        }
                    }
                }
            }

        }

        if (!isKeyed) {
            EmbeddingStructure emb = null;
            if (conf.isPrivateMode()) {
                emb = (EmbeddingStructure) queryRecord.get(Record.PRIVATE_STRUCTURE);
            } else {
                emb = ((EmbeddingStructure[]) embMap.get(Configuration.RECORD_LEVEL))[0];
            }
            forkHashTables(emb, queryRecord, Configuration.RECORD_LEVEL, result);
        }

        return result;
    }

    public HashMap<String, BloomFilter[]> toBloomFilter(Record rec) {
        HashMap<String, BloomFilter[]> bfMap = new HashMap<String, BloomFilter[]>();
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        ArrayList<String> fieldNames = rec.getFieldNames();
        BloomFilter bf = new BloomFilter("", 1000, 10, 2, true);

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            boolean isNotIndexedField = rec.isNotIndexedField(fieldName);
            String s = (String) rec.get(fieldName);
            if (this.getConfiguration().isKeyed) {
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    if (keyFieldName.equals(fieldName)) {
                        Key key = this.getConfiguration().getKey(keyFieldName);
                        boolean isTokenized = key.isTokenized();
                        if (!isTokenized) {
                            bfMap.put(keyFieldName, new BloomFilter[]{new BloomFilter(s, key.size, 15, 2, true)});
                        } else {
                            String[] os = (String[]) rec.get(keyFieldName + Key.TOKENS);
                            BloomFilter[] bfs = new BloomFilter[os.length];
                            for (int k = 0; k < bfs.length; k++) {
                                String v = os[k];
                                bfs[k] = new BloomFilter(v, key.size, 15, 2, true);
                            }
                            bfMap.put(keyFieldName, bfs);
                        }
                    }
                }
            } else if (!isNotIndexedField) {
                bf.encode(s, true);
            }
        }
        if (!isKeyed) {
            bfMap.put(Configuration.RECORD_LEVEL, new BloomFilter[]{bf});
        }

        return bfMap;
    }

    public abstract String buildHashKey(int j, EmbeddingStructure struct, String keyFieldName);

    public abstract boolean distance(EmbeddingStructure struct1, EmbeddingStructure struct2, Key key);

    public abstract HashMap<String, ? extends EmbeddingStructure[]> createKeyFieldEmbeddingStructureMap(Record rec);

    public abstract Configuration getConfiguration();

}
