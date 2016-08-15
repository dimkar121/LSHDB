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

    public Result queryNodes(QueryRecord queryRecord, Result result) throws ExecutionException, StoreInitException, NoKeyedFieldsException {
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
                    public Result call() throws ExecutionException, StoreInitException, NoKeyedFieldsException {
                        Result r = null;
                        System.out.println("querying node " + node.server);
                        if ((! node.isLocal()) && (q.isClientQuery()) ) {  
                            Client client = new Client(node.server, node.port);
                            try {
                                q.setServerQuery();
                                r = client.queryServer(q);
                                if (r.getStatus() == Result.STORE_NOT_FOUND) {
                                    throw new StoreInitException("The specified store " + q.getStoreName() + " was not found on "+node.server);
                                }
                                r.setRemote();
                            } catch (ConnectException ex) {
                                System.out.println(Client.CONNECTION_ERROR_MSG);
                                System.out.println("Specified server: " + node.server);
                                System.out.println("You should either check its availability, or resolve any possible network issues.");
                            } catch (UnknownHostException ex) {
                                System.out.println(Client.UNKNOWNHOST_ERROR_MSG);
                            }   

                        } else {
                                r = query(q);
                                r.prepare();                                
                        }
                        return r;
                    }
                });
            }
        }

        try {
            List<Future<Result>> futures = executorService.invokeAll(callables);

            for (Future<Result> future : futures) {
                if (future != null) {
                    Result partialResults = future.get();
                    if (partialResults != null) {
                        for (int j = 0; j < partialResults.getRecords().size(); j++) {
                            Record rec = partialResults.getRecords().get(j);
                            if (partialResults.isRemote())
                                rec.setRemote();
                            result.getRecords().add(rec);
                        }
                    }
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        executorService.shutdown();
        return result;
    }

    public Result forkQuery(QueryRecord q) throws StoreInitException, NoKeyedFieldsException {
        Result r = null;
        try {
            r = queryNodes(q, new Result(q));
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        }
        return r;
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

    public abstract void hash(String id, Object data, String keyFieldName);

    public abstract Result query(QueryRecord queryRecord) throws NoKeyedFieldsException;

    public abstract void insert(Record rec);

    public abstract Configuration getConfiguration();

}
