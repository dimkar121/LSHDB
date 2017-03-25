/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.embeddables.BloomFilter;
import gr.eap.LSHDB.embeddables.StringEmb;
import java.security.InvalidParameterException;
import java.util.Random;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class GramKey  extends Key{
    final static Logger log = Logger.getLogger(GramKey.class);
    
    public static final long serialVersionUID = 501001L;
    
    public int t;   
    public int q=2;
    public char padChar;
    public int[][] samples;
    String[] tokens;
    int times;
    
    public GramKey(String keyFieldName, double delta,int t, int q, char padChar, boolean tokenized,boolean performComparisons, Embeddable emb){
        this.keyFieldName = keyFieldName;
        this.k = k;
        this.delta = delta;    
        this.t = t;                
        this.q = q;
        this.padChar = padChar;
        optimizeL();  
        this.tokenized = tokenized;
        log.info("Number of hash tables generated L="+this.L+" using k="+this.k+" and size="+this.size);
        
        this.performComparisons = performComparisons;     
        //if (emb==null)
          //  throw new StoreInitException("Embeddable object cannot be null.");
        setEmbeddable(emb);
    }
    
    public GramKey(String keyFieldName) {       
      this(keyFieldName,.1,2,2,'%',true,true, new StringEmb(2,'%'));
    }
    
    public GramKey(int k, double delta, int t) {       
        this.k = k;
        this.delta = delta;    
        this.t = t; 
        this.padChar = '%';
        optimizeL();  
    }
    
    
    
    public void setTimes(int times){
        this.times = times;
    }
        
    @Override
    public int optimizeL() {
        this.L = 1;
        return 1;
    }
    
    @Override    
    public Key create(double thresholdRatio){
        int t =  (int) Math.round(this.t * thresholdRatio);  
        return new GramKey(k,delta,t);
    }
    
   
}
