package gr.eap.LSHDB;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class NGram {

    public static ArrayList<String> getGrams(String word, int n) {
        ArrayList<String> ngrams = new ArrayList();
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if (i > (n - 2)) {
                String ng = "";
                for (int j = n - 1; j >= 0; j--) {
                    ng = ng + word.charAt(i - j);
                }
                ngrams.add(ng);
            }
        }
        return ngrams;
    }

  /*  public static HashMap<String, Integer> getHashBigrams() {
        String s1 = "abcdefghijklmnopqrstuvwxyz";
        int a = 0;
        HashMap<String, Integer> map = new HashMap();
        for (int i = 0; i < s1.length(); i++) {
            char c1 = s1.charAt(i);
            for (int j = 0; j < s1.length(); j++) {
                char c2 = s1.charAt(j);
                String s2 = new String(new char[]{c1, c2});
                List<String> al = getGrams(s2, 2);
                for (String s : al) {
                    map.put(s, a);
                    a++;
                }

            }
        }        
        return map;
    }*/

  

    
   
}
