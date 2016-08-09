/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.client;

/**
 *
 * @author 
 * 
 */
import gr.eap.LSHDB.priv.*;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Client {
    

    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream reply;
    private boolean isConnected = false;
    String server;
    int port;
    String dbName;
    
    public Client(String server,int port,String dbName) {        
            this.server = server;        
            this.port=port;
            this.dbName=dbName;
    }

    public Result queryServer(QueryRecord query) throws IOException, ClassNotFoundException{
       try{ 
        socket = new Socket(server, port); 
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(query);
        reply = new ObjectInputStream(socket.getInputStream());
        Result result = (Result) reply.readObject();
        socket.close();        
        return result;
       }catch(ConnectException cex){
             System.out.println("Remote LSHDB instance "+server+" cannot be reached.");
       } 
       return null;
    }
    
    public Object submitCommand(String cmd){
     try {
            Socket socket = new Socket(server, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            
            outputStream.writeObject(cmd);
            ObjectInputStream  reply = new ObjectInputStream(socket.getInputStream());
            Object response =  reply.readObject();            
            outputStream.close();
            reply.close();
            socket.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }
    

    
}
