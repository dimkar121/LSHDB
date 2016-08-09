/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.priv.Client;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;

/**
 *
 * @author dimkar
 */
public class RemoteQuery extends Thread {
    
    Node node;
    QueryRecord queryRecord;
    Result result;
    Result partialResults;
    
    
    public Result getResults(){
        return this.result;
    }
    
    public RemoteQuery(Node node, QueryRecord queryRecord){        
         this.node=node;        
         this.queryRecord = queryRecord;
    }
    
    public Result mergeResult(Result result){
        this.result = result;
        merge();
        return this.result;
    }
    
    
    public synchronized void merge(){
        if (partialResults!=null)
        for (int j = 0; j < partialResults.getRecords().size(); j++) {
                Record rec = partialResults.getRecords().get(j);
                result.getRecords().add(rec);
                System.out.println("REMOTE=" + rec.getId());
          }
    }
    
    
    public void run() {
        try {
            Client client = new Client(node.ip, node.port, queryRecord.getDbName());            
            partialResults = client.queryServer(queryRecord);
         } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
}
