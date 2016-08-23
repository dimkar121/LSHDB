/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Config;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class Server {

    final static Logger log = Logger.getLogger(Server.class);

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private String alias = null;
    private String url = null;
    private int port = 4443;
    private ObjectInputStream inStream = null;
    Configuration[] hc;
    DataStore[] lsh;
    int c = 0;

    private ExecutorService workerExecutor = Executors.newCachedThreadPool();
    
    
    public Server(String configDir) {

        boolean queryRemoteNodes = true;
        Config config = new Config(Config.CONFIG_FILE);
        alias = config.get(Config.CONFIG_ALIAS);
        try {
            port = Integer.parseInt(config.get(Config.CONFIG_PORT));
        } catch (NumberFormatException ex) {
            log.error("Invalid port number specified=" + port, ex);
            port = 4443;
        }
        String[] dbNames = config.getList(Config.CONFIG_STORE_NAME);
        String[] targets = config.getList(Config.CONFIG_TARGET);
        String[] dbEngines = config.getList(Config.CONFIG_NOSQL_ENGINE);
        String[] LSHStores = config.getList(Config.CONFIG_LSH);
        String[] LSHConf = config.getList(Config.CONFIG_CONFIGURATION);
        String[] remoteAliases = config.get(0, Config.CONFIG_REMOTE_NODES, Config.CONFIG_ALIAS);
        String[] remoteNodeUrls = config.get(0, Config.CONFIG_REMOTE_NODES, Config.CONFIG_URL);
        String[] ports = config.get(0, Config.CONFIG_REMOTE_NODES, Config.CONFIG_PORT);
        String[] enableds = config.get(0, Config.CONFIG_REMOTE_NODES, Config.CONFIG_ENABLED);

        HashMap<String, ArrayList<String>> aliasMap = new HashMap<String, ArrayList<String>>();
        if (remoteAliases != null) {
            for (int i = 0; i < remoteAliases.length; i++) {
                ArrayList<String> confs = new ArrayList<String>();
                confs.add(remoteNodeUrls[i]);
                confs.add(ports[i]);
                confs.add(enableds[i]);
                aliasMap.put(remoteAliases[i], confs);
            }
        }

        hc = new Configuration[dbNames.length];
        lsh = new DataStore[dbNames.length];
        for (int i = 0; i < dbNames.length; i++) {
            try {
                hc[i] = DataStoreFactory.build(targets[i], dbNames[i], LSHConf[i], dbEngines[i], false);
                lsh[i] = DataStoreFactory.build(targets[i], dbNames[i], LSHStores[i], dbEngines[i], hc[i], false);
                String[] remoteStores = config.get(i, Config.CONFIG_STORE, Config.CONFIG_ALIAS);
                lsh[i].setQueryMode(true);
                for (int a = 0; a < remoteStores.length; a++) {
                    String alias = remoteStores[a];
                    if (aliasMap.containsKey(alias)) {
                        ArrayList<String> confs = aliasMap.get(alias);
                        String url = confs.get(0);
                        int port = Integer.parseInt(confs.get(1));
                        boolean enabled = Boolean.parseBoolean(confs.get(2));
                        lsh[i].addNode(new Node(alias, url, port, enabled));
                        log.info("Registering remote node " + alias + " at " + url + " for " + dbNames[i]);
                    }
                }
                Node localNode = new Node(this.alias, "", port, true);
                localNode.setLocal();
                lsh[i].addNode(localNode);
                log.info("Supporting " + dbNames[i] + " which uses " + LSHStores[i] + " and is materialized by " + dbEngines[i]);
            } catch (ClassNotFoundException ex) {
                log.error("Decalred classes " + LSHConf[i]+" and/or "+LSHStores[i] + " not found.",ex);
            } catch (NoSuchMethodException ex) {
                log.error("The particular constructor cannot be found in the decalred classes " + LSHConf[i]+" and/or "+LSHStores[i] +".",ex);
            }
        }
        log.info("LSHDB instance started at " + LocalDateTime.now());

    }

    public String getLocalServerAlias() {
        return alias;
    }

    public void communicate() {
        try {
            serverSocket = new ServerSocket(port, 100);

            while (true) {
                c++;
                Worker worker = new Worker("t-" + c, serverSocket.accept(), lsh);
                workerExecutor.execute(worker);
            }
            
            
        } catch (SocketException ex) {
            log.error("Waiting for connections: Socket error", ex);

        } catch (IOException ex) {
            log.error("Waiting for connections: IO error", ex);

        }
        this.workerExecutor.shutdown();
    }

    public static void main(String[] args) {
        String configDir = "";
        if (args.length > 0) {
            configDir = args[0];
        }
        Server server = new Server(configDir);
        server.communicate();        
    }
}
