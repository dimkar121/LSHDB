/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import java.io.Serializable;

/**
 *
 * @author dimkar
 */
public class QueryValueConf implements Serializable{
     double userPercentageThreshold=0.0;
     boolean performComparisons;
     
     public QueryValueConf(double userPercentageThreshold, boolean performComparisons){
         this.userPercentageThreshold = userPercentageThreshold;
         this.performComparisons = performComparisons;
         
     }
     
     public double getUserPercentageThreshold(){
          return userPercentageThreshold;
     }
     
     public boolean performComparisons(){
          return this.performComparisons;
     }
}
