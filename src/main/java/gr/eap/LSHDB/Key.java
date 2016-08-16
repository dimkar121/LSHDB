/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

/**
 *
 * @author dimkar
 */
public abstract class Key implements Serializable{
    
    public static final long serialVersionUID = 50L;
    
    int L;
    int k;
    int size;
    double delta; 
    String keyFieldName;
    boolean tokenized;
    boolean performComparisons;
    public static String KEYFIELD = "_keyField_";
    public static String TOKENS = "_tokens";
    
    public double thresholdRatio;
    
    public abstract void optimizeL();
    
    public String getKeyFieldName(){
        return keyFieldName;
    }
    
    public boolean isTokenized(){
        return tokenized;
    }
    
    public boolean performComparisons(){
        return this.performComparisons;
    }
    
    
    
   
    
}
