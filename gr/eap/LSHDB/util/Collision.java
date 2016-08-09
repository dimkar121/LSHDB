/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.util;

import gr.eap.LSHDB.util.Record;
import java.io.Serializable;

/**
 *
 * @author dimkar
 */
public class Collision  implements Serializable{
    public String Id;
    int collisions;
    Record dataRecord;
    
    public Collision(Record r, int collisions){
        this.Id = r.getId();
        this.dataRecord = r;
        this.collisions = collisions;
    }
    
    public Record getRecord(){
        return dataRecord;
    }
    
    public int getCollisionNo(){
        return collisions;
    }
    
    public void setCollisionNo(int collisionNo){
        this.collisions=collisions;
    }
    
    
    @Override    
    public boolean equals(Object obj) {
      if (!(obj instanceof Collision))
            return false;
        if (obj == this)
            return true;  
      String id = ((Collision) obj).Id;
      if (this.Id.equals(id)) return true;
      else 
         return false;
    }
    
    @Override
    public int hashCode() {
        return this.Id.hashCode();
    }

    @Override    
    public String toString() {       
       return this.Id;
    }
    
}
