/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

/**
 *
 * @author dimkar An object of type Client_RDS lives in G and sublits queries to
 * each data custodian.
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
    

    public static void main(String[] args) {
        Client client = new Client("83.212.115.69",4443,"testV");
        try {
            
            String fileName = "c://voters//test_voters_B.txt";
            FileReader input1 = new FileReader(fileName);
            BufferedReader bufRead1 = new BufferedReader(input1);
            int lines = 10; //lsh.countLines(file);
            
            for (int i = 0; i < lines; i++) {
                   
                String line1 = bufRead1.readLine();
                StringTokenizer st1 = new StringTokenizer(line1, ",");
                String id = st1.nextToken(); //id
                String lastName = st1.nextToken();
                String firstName = st1.nextToken();
                String address = st1.nextToken();
                String town = st1.nextToken();
                QueryRecord rec = new QueryRecord("testV",10);
                rec.setId(id);
                rec.set("First Name", firstName);
                rec.set("Last Name", lastName);
                rec.set("Address", address);
                rec.set("Town", town);
                //lsh.query(rec);
                Result result = client.queryServer(rec);
                ArrayList<Record> recs = result.getRecords();
                for (int j = 0; j < recs.size(); j++) {
                    Record record = recs.get(j);
                    for (int x = 0; x < record.getFieldNames().size(); x++) {
                        String field = record.getFieldNames().get(x);
                        String value = (String) record.get(field);
                        System.out.println(field + "=" + value);
                    }
                    System.out.println("=======================================================");
                }


                //client.socket.close();
                //Thread.sleep(60);

            }
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
    }
}
