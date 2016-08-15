/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.Config;
import gr.eap.LSHDB.util.Property;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 *
 * @author dimkar
 */
public class Server {
    
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private String server = null;
    private int port = 4443;
    private ObjectInputStream inStream = null;
    Configuration[] hc;
    DataStore[] lsh;
    int c = 0;

    public Server(String configDir) {
        Properties props = new Properties();
        FileInputStream in;

        String[] folders = null;
        String[] dbNames = null;
        boolean queryRemoteNodes = true;
        String[] dbEngines = null;
        String[] LSHConf = null;
        String[] LSHStores = null;
        //String[] remoteNodes = null;
        String[] indexFieldNames = null;
        Config config = new Config(configDir+"config.xml");
        server = config.get("server");
        port = Integer.parseInt(config.get("port"));
        dbNames = config.getList("name");
        folders = config.getList("folder");
        dbEngines = config.getList("engine");
        LSHStores = config.getList("LSHStore");
        LSHConf = config.getList("LSHConfiguration");        



        hc = new Configuration[dbNames.length];
        lsh = new DataStore[dbNames.length];

        for (int i = 0; i < dbNames.length; i++) {
            hc[i] = LSHStoreFactory.build(folders[i], dbNames[i], LSHConf[i], dbEngines[i], false);
            lsh[i] = LSHStoreFactory.build(folders[i], dbNames[i], LSHStores[i], dbEngines[i], hc[i], false);


            String[] remoteNodes = config.get(i, "remote_nodes", "remote_node", "server");
            String[] ports = config.get(i, "remote_nodes", "remote_node", "port");
            String[] enableds = config.get(i, "remote_nodes", "remote_node", "enabled");

            //hc[i] = new HammingConfiguration(folders[i], dbNames[i],dbEngines[i],false);
            //lsh[i] = new HammingLSHStore(folders[i], dbNames[i],dbEngines[i],(HammingConfiguration) hc[i],false);            

            if (remoteNodes != null) {
                lsh[i].setQueryMode(true);
                for (int a = 0; a < remoteNodes.length; a++) {
                    String remoteNode = remoteNodes[a];
                    int port = Integer.parseInt(ports[a]);
                    boolean enabled = Boolean.parseBoolean(enableds[a]);
                    lsh[i].addNode(new Node(remoteNode, port, enabled));
                    System.out.println("registering remote node " + remoteNode+ " for " + dbNames[i]);
                }
                Node localNode = new Node(server, port, true);
                localNode.setLocal();
                lsh[i].addNode(localNode);
            }
            System.out.println("Supporting " + dbNames[i] + " which uses " + LSHStores[i] + " and is materialized by " + dbEngines[i]);

        }

    }

    
    public String getServerName(){
        return server;
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
        if (args.length > 0)
            configDir = args[0];
        Server server = new Server(configDir);
        server.communicate();
    }
}
