/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

import java.util.Random;
import org.apache.commons.math3.distribution.BinomialDistribution;

/**
 *
 * @author dimkar
 */
public class HammingKey  extends Key{
    public int t;    
    public int[][] samples;
    String[] tokens;
    
    public HammingKey(String keyFieldName, int k,double delta,int t, int size, boolean tokenized,boolean performComparisons){
        this.keyFieldName = keyFieldName;
        this.k = k;
        this.delta = delta;    
        this.t = t;
        this.size = size;
        optimizeL();  
        this.tokenized = tokenized;
        System.out.println("L="+this.L);
        this.samples = new int[this.L][this.k];
        initSamples(); 
        this.performComparisons = performComparisons;      
    }
    
    public HammingKey(String keyFieldName){
        this(keyFieldName,30,.1,75,700,true,true);
    }
    
    public void optimizeL() {
        L = (int) Math.ceil(Math.log(delta) / Math.log(1 - Math.pow((1.0 - (t * 1.0 / size)), k)));
    }
    
    public int getLc() {
        double p = 1 - (t * 1.0) / (size * 1.0);
        p = Math.pow(p, k);
        double exp = (L * p);
        double std = Math.sqrt(exp * (1 - p));
        int C=(int) Math.round(exp-std); 
        
        double x = (Math.sqrt(Math.log(delta) * Math.log(delta) - 2 * C * Math.log(delta)) - Math.log(delta) + C) / p;
        int Lc = (int) Math.ceil(x);
        double b=Lc*p;
        if (C > b) System.out.println("does not apply C > np.");
        BinomialDistribution bd1 = new BinomialDistribution(L, p);          
        for (int l=L;l<L*2;l++){
           bd1 = new BinomialDistribution(l, p);                  
           double result = bd1.cumulativeProbability(C-1);
           if (result < delta){
               Lc=l;
               break;
           }    
        }   
        System.out.println("Lc reduced to="+Lc);
        return Lc;
    }
    
     public void initSamples() {
        Random r = new Random(System.currentTimeMillis());
        for (int j = 0; j < this.L; j++) {
            for (int k = 0; k < this.k; k++) {
                samples[j][k] = r.nextInt(this.size);
            }
        }
    }
}
