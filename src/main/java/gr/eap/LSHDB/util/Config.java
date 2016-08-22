/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author dimkar
 */
public class Config {

    final static Logger log = Logger.getLogger(Config.class);

    public final static String CONFIG_FILE = "config.xml";
    public final static String CONFIG_ALIAS = "alias";
    public final static String CONFIG_PORT = "port";
    public final static String CONFIG_STORE_NAME = "name";
    public final static String CONFIG_STORE = "store";
    public final static String CONFIG_TARGET = "target";
    public final static String CONFIG_NOSQL_ENGINE = "engine";
    public final static String CONFIG_EMBEDDABLE = "embeddable";
    public final static String CONFIG_LSH = "LSHStore";
    public final static String CONFIG_CONFIGURATION = "LSHConfiguration";
    public final static String CONFIG_REMOTE_NODES = "remote_nodes";
    public final static String CONFIG_URL = "url";
    public final static String CONFIG_ENABLED = "enabled";

    Document document;

    public Config(String fileName) {
        try {
            URI uri = getClass().getClassLoader().getResource(fileName).toURI();
            File file = new File(uri);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file);
        } catch (ParserConfigurationException | SAXException ex) {
            log.error("XML parsing error during processing config.xml", ex);
        } catch (IOException | URISyntaxException ex) {
            log.error("Config.xml not found. ", ex);
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

    public String[] get(int i, String... tags) {
        //String[] v = new String[getCount(i,pre1,pre2)];
        ArrayList<String> v = new ArrayList<String>();
        Node doc = document.getElementsByTagName(tags[0]).item(i); //store
        Element el = (Element) doc;
        String[] a = null;
        if (el != null) {
            NodeList nodes = el.getElementsByTagName(tags[1]);
            a = new String[nodes.getLength()];
            for (int k = 0; k < nodes.getLength(); k++) {
                Node node = nodes.item(k);
                a[k] = node.getTextContent();
                //System.out.println(node.getNodeValue()+" "+node.getTextContent()+" "+node.getNodeName());
            }
        }
        return a;
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
    
    public StoreConfigurationParams get(String key, String value) {
        NodeList nodes = document.getElementsByTagName(key);
        StoreConfigurationParams c = new StoreConfigurationParams();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node =(Element) nodes.item(i);            
            Node nameNode = node.getElementsByTagName(Config.CONFIG_STORE_NAME).item(i);            
            if (nameNode.getTextContent().equals(value)){
                Node targetNode = node.getElementsByTagName(Config.CONFIG_TARGET).item(i);                        
                c.setTarget(targetNode.getTextContent());
                Node engineNode = node.getElementsByTagName(Config.CONFIG_NOSQL_ENGINE).item(i);            
                c.setEngine(engineNode.getTextContent());
                Node configurationNode = node.getElementsByTagName(Config.CONFIG_CONFIGURATION).item(i);            
                c.setConfiguration(configurationNode.getTextContent());
                log.info(c.getEngine()+" "+c.getTarget()+" "+c.configuration);
                return c;
            }    
        }
        return null;
    }
    
    
}
