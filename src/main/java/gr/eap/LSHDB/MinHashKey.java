/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import gr.eap.LSHDB.embeddables.BloomFilter;
import java.util.Random;

/**
 *
 * @author dimkar
 */
public class MinHashKey extends Key{
    
    public static final long serialVersionUID = 502L;
    
    public double t;    
    public int permutationsNo = 0;
    public int[][][] permutations;
    String[] tokens;
    
    
    
    public MinHashKey(String keyFieldName, int k, double delta, double t, boolean tokenized, boolean performComparisons, Embeddable emb){
        this.keyFieldName = keyFieldName;
        this.k = k;
        this.delta = delta;    
        this.t = t;
        this.size = emb.getSize();
        optimizeL();  
        this.tokenized = tokenized;
        this.permutationsNo = this.L * this.k;
        this.permutations = new int[this.L][this.k][this.size];
        initPermutations(); 
        setEmbeddable(emb);
    }
    
    public MinHashKey(String keyFieldName){
        this(keyFieldName,5,.1,.60,true,true, new BloomFilter(700,15,2));
    }
    
    
    public MinHashKey(int k, double delta, double t) {       
        this.k = k;
        this.delta = delta;    
        this.t = t;        
        optimizeL();  
    }
    
    
    
    @Override    
    public Key create(double thresholdRatio){
        double t =  this.t * thresholdRatio;                
        return new MinHashKey(k,delta,t);
    }   
    
    @Override
    public int optimizeL(){
        double p1 = Math.pow(1.0 - t, k);
        L = (int) Math.ceil(Math.log(delta) / Math.log(1 - p1));
        return L;
    }
    
    
    
    
    public void initPermutations() {
        for (int l = 0; l < this.L; l++) 
           for (int i = 0; i < this.k; i++)          
            for (int j = 0; j < this.size; j++) {
                permutations[l][i][j] = j;
            }
        
        // shuffle
        for (int l = 0; l < this.L; l++) 
           for (int i = 0; i < this.k; i++)             
            for (int j = 0; j < size; j++) {
                // int from remainder of deck
                int r = j + (int) (Math.random() * (size - j));
                int swap = permutations[l][i][r];
                permutations[l][i][r] = permutations[l][i][j];
                permutations[l][i][j] = swap;
            }
      }
    
 }
    

