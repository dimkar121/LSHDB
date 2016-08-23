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
package gr.eap.LSHDB.util;

/**
 *
 * @author dimkar
 */
public class StoreConfigurationParams {
    String target;
    String name;
    String engine;
    String configuration;
    String LSHStore;
    public String getLSHStore(){
        return LSHStore;
    }
    
    public String getTarget(){
        return target;
    }
    public String getEngine(){
        return engine;
    }
    public String getConfiguration(){
        return engine;
    }
    public void setTarget(String target){
        this.target = target;
    }
    public void setEngine(String engine){
        this.engine = engine;
    }
    public void setConfiguration(String configuration){
        this.configuration = configuration;
    }
    public void setLSHStore(String LSHStore){
        this.LSHStore = LSHStore;
    }
}
