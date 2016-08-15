/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author dimkar
 */
public class Node {

    String server;
    int port;
    boolean enabled = true;
    boolean local = false;
    
    public boolean isEnabled(){
        return (enabled==true);
    }
    
    public boolean isLocal(){
        return local;
    }
    
    public void setLocal(){
        this.local=true;
    }
    
    public Node(String server, int port, boolean enabled) {
        this.server = server;
        this.port = port;
        this.enabled = enabled;
    }

    
    
    
    
    
}

    

