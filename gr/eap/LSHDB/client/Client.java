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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Client {

    public static String CONNECTION_ERROR_MSG = "Remote LSHDB instance cannot be reached. ";
    public static String UNKNOWNHOST_ERROR_MSG = "The specified remote LSHDB instance cannot be resolved or determined. ";
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream reply;
    private boolean isConnected = false;
    String server;
    int port;

    public Client(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public Result queryServer(QueryRecord query) throws ConnectException, UnknownHostException {
        try {
            socket = new Socket(server, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(query);
            reply = new ObjectInputStream(socket.getInputStream());
            Result result = (Result) reply.readObject();
            socket.close();
            return result;
        } catch (ConnectException cex) {
            throw cex;
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (ClassNotFoundException cnfex) {
            cnfex.printStackTrace();
        }
        return null;
    }

    public Object submitCommand(String cmd) throws ConnectException, UnknownHostException {
        try {
            Socket socket = new Socket(server, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(cmd);
            ObjectInputStream reply = new ObjectInputStream(socket.getInputStream());
            Object response = reply.readObject();
            outputStream.close();
            reply.close();
            socket.close();
            return response;
        } catch (ConnectException cex) {
            throw cex;
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (ClassNotFoundException cnfex) {
            cnfex.printStackTrace();
        }
        return null;
    }

}
