/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

/**
 *
 * @author dimkar
 */
public class EchoClient {

    public static void main(String[] args) throws IOException {

        Socket pingSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            pingSocket = new Socket("localhost", 4443);
            in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));

            ObjectOutputStream outputStream = new ObjectOutputStream(pingSocket.getOutputStream());
            
            outputStream.writeObject("SHOW STATS");
            ObjectInputStream  reply = new ObjectInputStream(pingSocket.getInputStream());
            StringBuffer response = (StringBuffer) reply.readObject();
            System.out.println(response.toString());
            
            outputStream.writeObject("GET INDEXED DATABASES");
            reply = new ObjectInputStream(pingSocket.getInputStream());
            Vector<String> dbs = (Vector<String>) reply.readObject();
             
            
            outputStream.close();
            reply.close();
            pingSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
