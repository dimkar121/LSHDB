/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.Record;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import gr.eap.LSHDB.client.Client;
import gr.eap.LSHDB.util.Config;
import gr.eap.LSHDB.util.ListUtil;
import gr.eap.LSHDB.util.StoreConfigurationParams;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public abstract class DataStore {

    final static Logger log = Logger.getLogger(DataStore.class);

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

    public final static String KEYS = "keys";
    public final static String DATA = "data";
    public final static String CONF = "conf";
    public final static String RECORDS = "records";

    public final static int NO_FORKED_HASHTABLES = 10;

    private ExecutorService hashTablesExecutor = Executors.newFixedThreadPool(600);
    private ExecutorService nodesExecutor = Executors.newCachedThreadPool();

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
        keyMap.put(fieldName, StoreEngineFactory.build(folder, dbName, KEYS + "_" + fieldName, dbEngine, massInsertMode));
    }

    public StoreEngine getDataMap(String fieldName) {
        fieldName = fieldName.replaceAll(" ", "");
        return dataMap.get(fieldName);
    }

    public void setDataMap(String fieldName, boolean massInsertMode) throws NoSuchMethodException, ClassNotFoundException {
        fieldName = fieldName.replaceAll(" ", "");
        dataMap.put(fieldName, StoreEngineFactory.build(folder, dbName, DATA + "_" + fieldName, dbEngine, massInsertMode));
    }

    /*
     public DataStore(StoreEngine db){
     if ((this.getConfiguration() != null) && (this.getConfiguration().isKeyed())) {
     String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
     for (int j = 0; j < keyFieldNames.length; j++) {
     String keyFieldName = keyFieldNames[j];
                    
     }
     } else {
     keys = 
     data = 
     keyMap.put(Configuration.RECORD_LEVEL, keys);
     dataMap.put(Configuration.RECORD_LEVEL, data);

     }
     }*/
    public void init(String dbEngine, boolean massInsertMode) throws StoreInitException {
        try {
            this.dbEngine = dbEngine;
            pathToDB = folder + System.getProperty("file.separator") + dbName;
            records = StoreEngineFactory.build(folder, dbName, RECORDS, dbEngine, massInsertMode);
            if ((this.getConfiguration() != null) && (this.getConfiguration().isKeyed())) {
                String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    setKeyMap(keyFieldName, massInsertMode);
                    setDataMap(keyFieldName, massInsertMode);
                }
            } else {
                keys = StoreEngineFactory.build(folder, dbName, KEYS, dbEngine, massInsertMode);
                data = StoreEngineFactory.build(folder, dbName, DATA, dbEngine, massInsertMode);
                keyMap.put(Configuration.RECORD_LEVEL, keys);
                dataMap.put(Configuration.RECORD_LEVEL, data);

            }
        } catch (ClassNotFoundException ex) {
            throw new StoreInitException("Declared class " + dbEngine + " not found.");
        } catch (NoSuchMethodException ex) {
            throw new StoreInitException("The particular constructor cannot be found in the decalred class " + dbEngine + ".");
        }
    }

    public void close() {
        hashTablesExecutor.shutdown();
        nodesExecutor.shutdown();
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
        List<Callable<Result>> callables = new ArrayList<Callable<Result>>();

        final QueryRecord q = queryRecord;
        for (int i = 0; i < this.getNodes().size(); i++) {
            final Node node = this.getNodes().get(i);
            if (node.isEnabled()) {
                callables.add(new Callable<Result>() {
                    public Result call() {
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
                                if (r == null) {
                                    r = new Result(q);
                                }
                                r.setRemote();
                                r.setOrigin(node.alias);
                                r.setStatus(Result.NO_CONNECT);
                            }

                        } else if (node.isLocal()) {
                            try {
                                r = query(q);
                                r.setStatus(Result.STATUS_OK);
                                r.prepare();
                                r.setOrigin(node.alias);
                            } catch (NoKeyedFieldsException ex) {
                                if (r != null) {
                                    r = new Result(q);
                                }
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
            List<Future<Result>> futures = nodesExecutor.invokeAll(callables);

            for (Future<Result> future : futures) {

                if (future != null) {  //partialResults should not come null

                    partialResults = future.get();
                    if (partialResults != null) {
                        result.getRecords().addAll(partialResults.getRecords());
                        result.setStatus(partialResults.getOrigin(), partialResults.getStatus());
                    }
                }

            }
        } catch (ExecutionException | InterruptedException ex) {
            if (ex.getCause() != null) {
                String server = " ";
                if (partialResults != null) {
                    server = partialResults.getOrigin();
                }
                log.error("forkQuery error ", ex);
                if (ex.getCause() instanceof Error) {
                    log.fatal("forkQuery Fatal error occurred on " + server, ex);
                    Node node = getNode(server);
                    if (node != null) {
                        node.disable();
                    }
                }

            }
        }
        return result;
    }

    public int getThreadsNo() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.getThreadCount();
    }

    public Result forkHashTables(Embeddable struct1, final QueryRecord queryRec, String keyFieldName) {
        final Configuration conf = this.getConfiguration();
        final int maxQueryRows = queryRec.getMaxQueryRows();
        final boolean performComparisons = queryRec.performComparisons(keyFieldName);
        final double userPercentageThreshold = queryRec.getUserPercentageThreshold(keyFieldName);
        final StoreEngine keys = this.getKeyMap(keyFieldName);
        final StoreEngine data = this.getDataMap(keyFieldName);
        Key key = conf.getKey(keyFieldName);
        boolean isPrivateMode = conf.isPrivateMode();

        final String keyFieldName1 = keyFieldName;
        final Embeddable struct11 = struct1;

        final Key newKey = key.create(userPercentageThreshold);
        log.debug("L=" + key.getL() + " ratio=" + userPercentageThreshold + " new L=" + newKey.getL());
        int partitionsNo = newKey.getL() / NO_FORKED_HASHTABLES;
        if (newKey.getL() % NO_FORKED_HASHTABLES != 0) {
            partitionsNo++;
        }

        Instant start = Instant.now();
        final Result result = new Result(queryRec);
        for (int p = 0; p < partitionsNo; p++) {

            List<Callable<Result>> callables = new ArrayList<Callable<Result>>();
            final int noHashTable = p * NO_FORKED_HASHTABLES;
            callables.add(new Callable<Result>() {
                public Result call() throws StoreInitException, NoKeyedFieldsException {
                    for (int j = noHashTable; j < (noHashTable + NO_FORKED_HASHTABLES); j++) {
                        if (j == newKey.getL()) {
                            break;
                        }
                        String hashKey = buildHashKey(j, struct11, keyFieldName1);

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

                                result.incPairsNo();
                                if ((performComparisons) && (!result.getMap(keyFieldName1).containsKey(id))) {
                                    Embeddable struct2 = (Embeddable) data.get(id);
                                    if (distance(struct11, struct2, newKey)) {
                                        result.add(keyFieldName1, dataRec);
                                        int matchesNo = result.getDataRecordsSize(keyFieldName1);
                                        if (matchesNo >= maxQueryRows) {
                                            return result;
                                        }
                                    } else {
                                    }

                                } else {
                                    result.add(keyFieldName1, dataRec);
                                }

                            }

                        }

                    }
                    return result;
                }

            });

            try {
                List<Future<Result>> futures = hashTablesExecutor.invokeAll(callables);
                Instant end = Instant.now();

                if (result.getRecords().size() >= maxQueryRows) {
                    throw new MaxNoRecordsReturnedException("Limit of returned records exceeded. No=" + result.getRecords().size());
                }

                /* int k = 0;
                 for (Future<Result> future : futures) {
                 if (future != null) {
                 Result partialResults = future.get();
                 k++;
                 if (partialResults != null) {
                 partialResults.prepare();
                 result.getRecords().addAll(partialResults.getRecords());
                 if (result.getRecords().size() >= maxQueryRows){
                 throw new MaxNoRecordsReturnedException("Limit of returned records exceeded. No="+result.getRecords().size());
                 }
                 }
                 }
                 }*/
                if (Duration.between(start, end).getSeconds() > 4) {
                    return result;
                }

            } catch (InterruptedException ex) {
                log.error("forkHashTables ", ex);
            } catch (MaxNoRecordsReturnedException ex) {
                return result;
            }

        }

        return result;
    }

    public void setHashKeys(String id, Embeddable emb, String keyFieldName) {
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
            Embeddable emb = (Embeddable) rec.get(Record.PRIVATE_STRUCTURE);
            data.set(rec.getId(), emb);
            setHashKeys(rec.getId(), emb, Configuration.RECORD_LEVEL);
            return;
        }

        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        HashMap<String, ? extends Embeddable[]> embMap = buildEmbeddableMap(rec);

        if (isKeyed) {
            for (int i = 0; i < keyFieldNames.length; i++) {
                String keyFieldName = keyFieldNames[i];
                Embeddable[] embs = embMap.get(keyFieldName);
                for (int j = 0; j < embs.length; j++) {
                    Embeddable emb = embs[j];
                    setHashKeys(rec.getId() + Key.KEYFIELD + j, emb, keyFieldName);
                    this.getDataMap(keyFieldName).set(rec.getId() + Key.KEYFIELD + j, emb);
                }

            }
        } else {
            data.set(rec.getId(), ((Embeddable[]) embMap.get(Configuration.RECORD_LEVEL))[0]);
            setHashKeys(rec.getId(), ((Embeddable[]) embMap.get(Configuration.RECORD_LEVEL))[0], Configuration.RECORD_LEVEL);
        }

        records.set(rec.getId(), rec);
    }

    public Result query(QueryRecord queryRecord) throws NoKeyedFieldsException {
        Result result = null;
        Configuration conf = this.getConfiguration();
        StoreEngine hashKeys = keys;
        StoreEngine dataKeys = data;
        HashMap<String, ? extends Embeddable[]> embMap = null;
        if (!conf.isPrivateMode()) {
            embMap = buildEmbeddableMap(queryRecord);
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
                        Embeddable[] structArr = embMap.get(fieldName);
                        for (int k = 0; k < structArr.length; k++) {
                            result = forkHashTables(structArr[k], queryRecord, keyFieldName);

                        }
                    }
                }
            }

        }

        if (!isKeyed) {
            Embeddable emb = null;
            if (conf.isPrivateMode()) {
                emb = (Embeddable) queryRecord.get(Record.PRIVATE_STRUCTURE);
            } else {
                emb = ((Embeddable[]) embMap.get(Configuration.RECORD_LEVEL))[0];
            }
            result = forkHashTables(emb, queryRecord, Configuration.RECORD_LEVEL);
        }

        return result;
    }

    public HashMap<String, Embeddable[]> buildEmbeddableMap(Record rec) {

        HashMap<String, Embeddable[]> embMap = new HashMap<String, Embeddable[]>();
        boolean isKeyed = this.getConfiguration().isKeyed();
        String[] keyFieldNames = this.getConfiguration().getKeyFieldNames();
        ArrayList<String> fieldNames = rec.getFieldNames();
        Embeddable embRec = null;
        if ((!isKeyed) && (this.getConfiguration().getKey(Configuration.RECORD_LEVEL) != null)) {
            embRec = this.getConfiguration().getKey(Configuration.RECORD_LEVEL).getEmbeddable().freshCopy();
        }

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            boolean isNotIndexedField = rec.isNotIndexedField(fieldName);
            String s = (String) rec.get(fieldName);
            if (isKeyed) {
                for (int j = 0; j < keyFieldNames.length; j++) {
                    String keyFieldName = keyFieldNames[j];
                    if (keyFieldName.equals(fieldName)) {
                        Key key = this.getConfiguration().getKey(keyFieldName);
                        boolean isTokenized = key.isTokenized();
                        if (!isTokenized) {
                            Embeddable emb = key.getEmbeddable().freshCopy();
                            emb.embed(s);
                            embMap.put(keyFieldName, new Embeddable[]{emb});
                        } else {
                            String[] keyValues = (String[]) rec.get(keyFieldName + Key.TOKENS);
                            Embeddable[] bfs = new Embeddable[keyValues.length];
                            for (int k = 0; k < bfs.length; k++) {
                                String v = keyValues[k];
                                Embeddable emb = key.getEmbeddable().freshCopy();
                                emb.embed(v);
                                bfs[k] = emb;
                            }
                            embMap.put(keyFieldName, bfs);
                        }
                    }
                }
            } else if (!isNotIndexedField) {
                if (embRec != null) {
                    embRec.embed(s);
                } else {
                    log.error("Although no key fields are specified, a record-level embeddable is missing.");
                }
            }
        }
        if (!isKeyed) {
            embMap.put(Configuration.RECORD_LEVEL, new Embeddable[]{embRec});
        }

        return embMap;
    }

    /*
     * Opens a HammingLSH store
     * found in specified @target.
     * @throws StoreInitExcoetion
     */
    public static DataStore open(String storeName) throws StoreInitException {
        Config conf = new Config(Config.CONFIG_FILE);
        StoreConfigurationParams c = conf.get(Config.CONFIG_STORE, storeName);
        if (c != null) {
            try {
                DataStore ds = DataStoreFactory.build(c.getTarget(), storeName, c.getLSHStore(), c.getEngine(), null, false);
                return ds;
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                log.error("Initialization error of data store " + storeName, ex);
            }
        }
        throw new StoreInitException("store " + storeName + " not initialized. Check config.xml ");
    }

    public abstract String buildHashKey(int j, Embeddable struct, String keyFieldName);

    public abstract boolean distance(Embeddable struct1, Embeddable struct2, Key key);

    public abstract Configuration getConfiguration();

}
