/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class BatchClient extends JFrame {

    private JTree tree;
    DefaultMutableTreeNode root;
    String fileName = "c://voters//test_voters_B.txt";
    String server = "localhost";
    //String server = "83.212.115.69";
    int port = 4443;
    boolean performComparisons = true;
    String dbName = "voters";
    int maxQueryRows = 50;
    Client client = new Client(server, port, dbName);

    public void query() throws FileNotFoundException, IOException, ClassNotFoundException {
        
        int lines = 20;//FileUtil.countLines(fileName);
        FileReader input1 = new FileReader(fileName);
        BufferedReader bufRead1 = new BufferedReader(input1);

        for (int i = 0; i < lines; i++) {            
            QueryRecord query = new QueryRecord(dbName, maxQueryRows);
            String line1 = bufRead1.readLine();
            StringTokenizer st1 = new StringTokenizer(line1, ",");
            String Id = st1.nextToken().trim(); //id            
            String lastName = st1.nextToken();
            String firstName = st1.nextToken();
            String address = st1.nextToken();

            Result r = null;
            if (st1.hasMoreTokens()) {
                String town = st1.nextToken();
                query.setId(Id); //may be name-based                                                              
                query.set("Last Name", lastName); //may be name-based                                              
                query.set("First Name", firstName); //may be name-based                                              
                query.set("Address", address); //may be name-based                                              
                query.set("Town", town); //may be name-based                                                                              

                query.set(1.0, true);
                System.out.println("Querying voters with Id=" + Id + " Last Name=" + lastName);

                try {
                    r = client.queryServer(query);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            if (r != null) {
                
                DefaultMutableTreeNode queryNode = new DefaultMutableTreeNode(Id);                
                DefaultMutableTreeNode queryDataNode1 = new DefaultMutableTreeNode(lastName);                
                DefaultMutableTreeNode queryDataNode2 = new DefaultMutableTreeNode(firstName);                
                queryNode.add(queryDataNode1);
                queryNode.add(queryDataNode2);
                
                for (int j = 0; j < r.getRecords().size(); j++) {
                    Record rec = r.getRecords().get(j);
                    DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(rec.getId());                
                    DefaultMutableTreeNode lastNameNode = new DefaultMutableTreeNode(rec.get("Last Name"));
                    DefaultMutableTreeNode firstNameNode = new DefaultMutableTreeNode(rec.get("First Name"));
                    dataNode.add(lastNameNode);
                    dataNode.add(firstNameNode);
                    queryNode.add(dataNode);
                }
                root.add(queryNode);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                tree.expandPath(new TreePath(model.getPathToRoot(queryNode.getParent())));
                model.reload();
            }

        }

    }

    public BatchClient() {
        //create the root node
        root = new DefaultMutableTreeNode("Root");
        
        tree = new JTree(root);
        JScrollPane dScrollPane = new JScrollPane(tree);
        //tree.setBackground(Color.LIGHT_GRAY);
        //tree.setRootVisible(false);
        this.add(dScrollPane);
        this.setSize(300, 700);
        this.setBackground(Color.gray);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Performing batch linkage");
        //this.pack();
        this.setVisible(true);


    }

    public static void main(String[] args) {
           BatchClient bc = new BatchClient();
           try{
               bc.query();
           }catch(Exception ex){
               ex.printStackTrace();
           }
    }
}
