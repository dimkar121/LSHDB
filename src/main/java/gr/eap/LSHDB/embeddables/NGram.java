package gr.eap.LSHDB.embeddables;

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

    
   
}
