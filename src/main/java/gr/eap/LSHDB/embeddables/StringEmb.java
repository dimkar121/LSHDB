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
package gr.eap.LSHDB.embeddables;

import gr.eap.LSHDB.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.log4j.Logger;

/**
 *
 * @author dimkar
 */
public class StringEmb implements Embeddable, Serializable, Cloneable {

    final static Logger log = Logger.getLogger(StringEmb.class);
    public static final long serialVersionUID = 67671001L;

    private String s;
    private int q;
    private char padChar;
    private ArrayList<String> grams;

    public StringEmb(int q, char padChar) {
        this.q = q;
        this.padChar = padChar;
    }

    public StringEmb(String s, int q , char padChar) {
        this.q = q;
        this.padChar = padChar;
        embed(s);
    }

    public ArrayList<String> getQGrams(){
         return this.grams;
    }
    
    public int getSize() {
        return this.s.length();
    }

    public void embed(Object v) throws ClassCastException {
        char[] chars = new char[this.q-1];
        Arrays.fill(chars, this.padChar);
        String padding = new String(chars);
        String s = ((String) v).toUpperCase();
        StringBuilder sb = new StringBuilder(padding);
        sb.append(s.toUpperCase());
        sb.append(padding);
        this.s = sb.toString();
        this.grams = getGrams(this.s,q);
    }

    public Embeddable freshCopy() {
        return new StringEmb("", this.q, this.padChar);
    }

    public String getValue() {
        return s;
    }

    public ArrayList<String> getGrams(String word, int n) {
        ArrayList<String> ngrams = new ArrayList();
        HashSet<String> set = new HashSet<String>();
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if (i > (n - 2)) {
                String ng = "";
                for (int j = n - 1; j >= 0; j--) {
                    ng = ng + word.charAt(i - j);
                }
                if (!set.contains(ng) && (ng != null)) {
                    set.add(ng);
                    ngrams.add(ng);
                }
            }
        }
        return ngrams;
    }
    
    
}
