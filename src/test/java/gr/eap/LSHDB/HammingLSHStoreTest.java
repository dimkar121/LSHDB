/*
 * Copyright 2016 dimkar.
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

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.year;
import gr.eap.LSHDB.util.Record;
import static jdk.nashorn.internal.runtime.Debug.id;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 *
 * @author dimkar
 */
public class HammingLSHStoreTest {
    HammingLSHStore lsh;
    String storeName = "dblp_test";

    
    public HammingLSHStoreTest() {
       
            DB mem = DBMaker.memoryDB().make();            
            HTreeMap map = mem.hashMap("store_test").create();
            //lsh = new HammingLSHStore();
            Record rec = new Record();
            rec.setId("1");
            rec.set("author", "Donald Knuth");
            rec.set("year", "2000");
            rec.set("title", "An Approximate COunting Method");
            

            
            
    }
    
    
    
    
    
    
    
    
}
