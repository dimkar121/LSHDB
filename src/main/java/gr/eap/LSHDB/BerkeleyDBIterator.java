/*
 * Copyright 2017 dimkar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.eap.LSHDB;

import com.sleepycat.je.Cursor;
import gr.eap.LSHDB.util.FileUtil;
import java.io.IOException;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import static gr.eap.LSHDB.BerkeleyDB.log;
import java.io.UnsupportedEncodingException;
import org.apache.log4j.Logger;
import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

/**
 *
 * @author dimkar
 */
public class BerkeleyDBIterator implements Iterable {
    final static Logger log = Logger.getLogger(BerkeleyDBIterator.class);

    Cursor cursor;
    DatabaseEntry foundKey;
    DatabaseEntry foundData;
    boolean hasNext = false;
    
     public BerkeleyDBIterator(Database db){
         try{
          cursor = db.openCursor(null, null);
         }catch(DatabaseException ex){
             log.error("Error opening BerkleleyDB iterator.");
         } 
     }
   
   public void seek(String partialKey){
      try{ 
        foundKey = new DatabaseEntry(partialKey.getBytes("UTF-8"));
        foundData = new DatabaseEntry();
        hasNext = (cursor.getSearchKeyRange(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
      }catch(UnsupportedEncodingException | DatabaseException ex){
          log.error("Error seeking BerkleleyDB iterator key "+partialKey);          
      } 
   }
   
   public void next(){
      try{
           hasNext = (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
       }catch(DatabaseException ex){
          log.error("Error next BerkleleyDB iterator.");  
      }  
   }
   
   public boolean hasNext(){
       return hasNext;
   }  
     
  
   
   public String getKey(){
        return  new String(foundKey.getData());
   }
    
   public Object getValue(){
       try{
          return FileUtil.deserialize(foundData.getData());           
       }catch(IOException | ClassNotFoundException ex){
             log.error("Error getting value BerkleleyDB iterator.");  
       }   
       return null;
   }
    
   
    public void close(){
     try{  
       cursor.close();
     }catch(DatabaseException ex){
        log.error("Error closing BerkleleyDB iterator.");  
     }
   }
 
}
