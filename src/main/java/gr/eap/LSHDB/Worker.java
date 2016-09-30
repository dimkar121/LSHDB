/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

/**
 *
 * @author dimkar
 */
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import gr.eap.LSHDB.util.ConfigurationQuery;
import gr.eap.LSHDB.util.ConfigurationReply;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;

public class Worker extends Thread {
final static Logger log = Logger.getLogger(Worker.class);

    private Socket socket = null;
    private QueryRecord query;
    private DataStore[] lsh;
    private Result result;
    private String name;

    public Worker(String name, Socket socket, DataStore[] lsh) {
        this.socket = socket;
        this.name = name;
        String msg = "Query "+ name +" initiated by: " + this.socket.getInetAddress().getHostAddress();
        log.info(msg);
        this.lsh = lsh;
    }

    public Result getResult() {
        return this.result;
    }

    public DataStore getDB(String storeName) {
        for (int i = 0; i < lsh.length; i++) {
            if (lsh[i].getStoreName().equals(storeName)) {
                return lsh[i];
            }
        }
        return null;
    }

    public void sendObject(Object response) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(response);
        outputStream.flush();
        outputStream.close();
    }

    public void sendMsgAsStream(String response) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.write(response);
        writer.flush();
        writer.close();
    }

    public Result queryStore() {
        try {
            result = new Result(this.query);
            String dsName = this.query.getStoreName();
            DataStore db = getDB(dsName);
            if (db == null) {
                result.setStatus(Result.STORE_NOT_FOUND);
                result.setMsg(Result.STORE_NOT_FOUND_ERROR_MSG);
            } else {

                long tStartInd = System.nanoTime();

                if (!this.query.isClientQuery()) {
                    result = db.query(this.query);
                    result.setStatus(Result.STATUS_OK);
                    result.prepare();
                } else {
                    result = db.forkQuery(query);
                }

                if (result == null) {
                    result = new Result(this.query);
                }
                long tEndInd = System.nanoTime();
                long elapsedTimeInd = tEndInd - tStartInd;
                double secondsInd = elapsedTimeInd / 1.0E09;
                result.setTime(secondsInd);
                String msg = "Query "+ this.name +" completed in " + secondsInd + " secs.";
                log.info(msg);
                result.prepare();
                        
            }

        } catch (NoKeyedFieldsException ex) {
            result.setStatus(Result.NO_KEYED_FIELDS_SPECIFIED);
            result.setMsg(Result.NO_KEYED_FIELDS_SPECIFIED_ERROR_MSG);
        }
        return result;
    }

    public void run() {
        Object stream = null;
        try {
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
            stream = inStream.readObject();

        } catch (StreamCorruptedException ex) {

            try {
                InputStream inStream = socket.getInputStream();

                String s = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, Charset.forName(StandardCharsets.UTF_8.name())));
                s = reader.readLine();
                stream = s;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {

            if (stream instanceof QueryRecord) {
                this.query = (QueryRecord) stream;
                Result result = queryStore();
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(result);
                outputStream.close();
            }

            if (stream instanceof ConfigurationQuery) {
                ConfigurationQuery conf = (ConfigurationQuery) stream;
                ConfigurationReply reply = new ConfigurationReply();
                int queryNo = conf.getQueryNo();
                if (queryNo == ConfigurationQuery.GET_KEYED_FIELDS) {
                    String dsStoreName = conf.getStoreName();
                    DataStore lsh = getDB(dsStoreName);
                    reply.setStatus(Result.STATUS_OK);
                    if (lsh == null) {
                        reply.setStatus(Result.STORE_NOT_FOUND);
                    } else {
                        reply.setReply(lsh.getConfiguration().getKeyFieldNames());
                    }
                }
                if (queryNo == ConfigurationQuery.GET_KEYED_STORES) {
                    Vector<String> dbs = new Vector<String>();
                    for (int i = 0; i < lsh.length; i++) {
                        if (lsh[i].getConfiguration().isKeyed()) {
                            dbs.add(lsh[i].getStoreName());
                        }
                    }
                    reply.setStatus(Result.STATUS_OK);
                    reply.setReply(dbs);
                }
                sendObject(reply);
            }

            if (stream instanceof String) {
                String request = (String) stream;
                //StringBuffer response = new StringBuffer("");
                String response = "";
                if (request.startsWith(JSON.JSON_REQUEST)) {
                    JSON json = new JSON(request);

                    QueryRecord query = json.buildQueryRecord();
                    double sim = json.getSimilarity();
                    Map<String, String> requestKeys = json.getRequestKeys();

                    DataStore db = getDB(json.getStoreName());
                    try {
                        if (db != null) {
                            if (requestKeys.entrySet().size() > 0) {
                                Iterator it = requestKeys.entrySet().iterator();
                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry) it.next();
                                    query.set(pair.getKey() + "", pair.getValue());

                                    if (db.getConfiguration().isKeyed) {
                                        String value = (String) pair.getValue();
                                        //String[] values = value.split(" ");

                                        query.set(pair.getKey() + "", value, sim, true);
                                        query.set(pair.getKey() + Key.TOKENS, new String[]{value}, sim, true);
                                    } else {
                                        query.set(pair.getKey() + "", pair.getValue());
                                    }

                                }
                            } else {
                                throw new JSONException(Result.NO_QUERY_VALUES_SPECIFIED, Result.NO_QUERY_VALUES_SPECIFIED_ERROR_MSG);
                            }
                            if (!db.getConfiguration().isKeyed) {
                                query.set(sim, true);
                            }
                        } else {
                            throw new JSONException(Result.STORE_NOT_FOUND, Result.STORE_NOT_FOUND_ERROR_MSG);
                        }
                        this.query = query;
                        Result result = queryStore();
                        response = json.prepare(result);

                    } catch (JSONException ex) {
                        HashMap h = json.prepareError(ex.error, ex.getMessage());
                        response = json.toJSON(h);
                    }

                    if (json.getCallBack() != null) {
                        StringBuffer sb = new StringBuffer(json.getCallBack());
                        sb.append(" ( ");
                        sb.append(response);
                        sb.append(" ) ");
                        response = sb.toString();
                    }

                }

                sendMsgAsStream(response);

            }

            socket.close();

        } catch (IOException ex2) {
            log.error("Streams ",ex2);
        }
    }
}
