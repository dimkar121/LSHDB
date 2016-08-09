/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 *
 * @author dimkar
 */
public class Config {

    Document document;

    public Config(String fileName) {
        File file = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String get(String key) {
        return document.getElementsByTagName(key).item(0).getTextContent().trim();
    }

    public int getCount(int i, String pre, String key) {
        Node node = document.getElementsByTagName(pre).item(i);
        int c = 0;
        if (node != null) {
            NodeList nodes = node.getChildNodes();
            for (int j = 0; j < nodes.getLength(); j++) {
                String name = nodes.item(j).getNodeName();
                if (name.equals(key)) {
                    c++;
                }
            }
        }
        return c;
    }

    public String[] get(int i, String pre1, String pre2, String key) {
        //String[] v = new String[getCount(i,pre1,pre2)];
        ArrayList<String> v = new ArrayList<String>();
        Node node = document.getElementsByTagName(pre1).item(i);
        if (node != null) {
            NodeList nodes = node.getChildNodes();   //remote_nodes of a certain dtore

            for (int j = 0; j < nodes.getLength(); j++) {
                Node node1 = nodes.item(j);
                for (int k = 0; k < node1.getChildNodes().getLength(); k++) {
                    Node node2 = node1.getChildNodes().item(k);
                    String s = node2.getNodeName().trim();
                    if (s.equals(key)) {
                        v.add(node2.getTextContent().trim());
                    }
                }
            }
        }
        String[] a = new String[v.size()];
        return v.toArray(a);
    }

    
    
    /*
     * get desc under the node
     * 
     */
    public String[] getList(String key) {
        NodeList nodes = document.getElementsByTagName(key);
        String[] names = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            names[i] = node.getTextContent().trim();
        }
        return names;
    }
}
