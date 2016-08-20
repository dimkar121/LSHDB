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

import java.io.Serializable;

/**
 *
 * @author dimkar
 */
public class ConfigurationReply implements Serializable{
    public static final long serialVersionUID = 20002L; 
    
     Object reply;
     int status;
     public ConfigurationReply(){
    }
     public void setReply(Object reply){
         this.reply = reply;
     }
     public Object getReply(){
         return reply;
     }
     
     public void setStatus(int status){
         this.status = status;
     }
     public int getStatus(){
         return status;
     }
     
}
