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

import static gr.eap.LSHDB.BerkeleyDBIterator.log;
import gr.eap.LSHDB.util.FileUtil;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.ReadOptions;

/**
 *
 * @author dimkar
 */
public class LevelDBIterator implements Iterable {
    final static Logger log = Logger.getLogger(LevelDBIterator.class);

    DBIterator dbIt;
    
     public LevelDBIterator(LevelDB de){
          ReadOptions ro = new ReadOptions();
          ro.fillCache();
          dbIt = de.db.iterator();
     }
   
   public void seek(String partialKey){
        dbIt.seek(bytes(partialKey));
   }
   
   public void next(){
       dbIt.next();
   }
   
   public boolean hasNext(){
       return dbIt.hasNext();
   }
   
     
   
   
   public String getKey(){
        return asString(dbIt.peekNext().getKey());
   }
    
   public Object getValue(){
       try{
          return FileUtil.deserialize(dbIt.peekNext().getValue());
       }catch(IOException | ClassNotFoundException ex){
              log.error("Error getting value LevelDB iterator.");
       }   
       return null;
   }
    
   
    public void close(){
     try{  
      dbIt.close();
     }catch(IOException ex){
           log.error("Error closing LevelDB iterator.");
     }
   }
 
}
