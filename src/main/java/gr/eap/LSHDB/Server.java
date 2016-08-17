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
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dimkar
 */
public class Server {

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private String alias = null;
    private String url = null;
    private int port = 4443;
    private ObjectInputStream inStream = null;
    Configuration[] hc;
    DataStore[] lsh;
    int c = 0;

    public Server(String configDir) {

        boolean queryRemoteNodes = true;
        Config config = new Config(configDir + "config.xml");
        alias = config.get("alias");
        port = Integer.parseInt(config.get("port"));
        String[] dbNames = config.getList("name");
        String[] folders = config.getList("folder");
        String[] dbEngines = config.getList("engine");
        String[] LSHStores = config.getList("LSHStore");
        String[] LSHConf = config.getList("LSHConfiguration");
        String[] remoteAliases = config.get(0, "remote_nodes", "alias");
        String[] remoteNodeUrls = config.get(0, "remote_nodes", "url");
        String[] ports = config.get(0, "remote_nodes", "port");
        String[] enableds = config.get(0, "remote_nodes", "enabled");

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
            hc[i] = LSHStoreFactory.build(folders[i], dbNames[i], LSHConf[i], dbEngines[i], false);
            lsh[i] = LSHStoreFactory.build(folders[i], dbNames[i], LSHStores[i], dbEngines[i], hc[i], false);
            String[] remoteStores = config.get(i, "store", "alias");
            lsh[i].setQueryMode(true);
            for (int a = 0; a < remoteStores.length; a++) {
                String alias = remoteStores[a];
                if (aliasMap.containsKey(alias)) {
                    ArrayList<String> confs = aliasMap.get(alias);
                    String url = confs.get(0);
                    int port = Integer.parseInt(confs.get(1));
                    boolean enabled = Boolean.parseBoolean(confs.get(2));
                    lsh[i].addNode(new Node(alias, url, port, enabled));
                    System.out.println("Registering remote node " + alias + " at " + url + " for " + dbNames[i]);
                }
            }
            Node localNode = new Node(this.alias, "", port, true);
            localNode.setLocal();
            lsh[i].addNode(localNode);
            System.out.println("Supporting " + dbNames[i] + " which uses " + LSHStores[i] + " and is materialized by " + dbEngines[i]);

        }

    }

    public String getLocalServerAlias() {
        return alias;
    }

    public void communicate() {
        try {
            serverSocket = new ServerSocket(port);

            while (true) {
                c++;
                Server_Thread thread1 = new Server_Thread("t-" + c, serverSocket.accept(), lsh);
                thread1.run();
            }

        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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
