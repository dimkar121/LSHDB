/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB;

/**
 *
 * @author dimkar
 */
/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.ArrayList;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BloomFilter extends EmbeddingStructure{

    private BitSet bitset;
    private int bitSetSize;
    private int bitsSet = 0;
    private int numberOfAddedElements;  // number of elements actually added to the Bloom filter
    private int k; // number of hash functions
    private int grams;
    private int[] cols;
    private int expectedNumberOfFilterElements = 0;
    private double bitsPerElement = 0.0;
    private double fp=.0;
    static final Charset charset = Charset.forName("UTF-8"); // encoding used for storing hash values as strings

    public BloomFilter(String s, int length, int k, int grams, boolean padded) {
        this.bitSetSize = length;
        this.bitset = new BitSet(bitSetSize);
        this.cols = new int[bitSetSize];
        this.k = k;
        this.grams = grams;
        this.bitsSet = 0;
        encode(s, true);
    }

    public BloomFilter(ArrayList<String> s, int length, int k, int grams, boolean padded) {
        this.bitSetSize = length;
        this.bitset = new BitSet(bitSetSize);
        this.cols = new int[bitSetSize];
        this.k = k;
        this.grams = grams;
        this.bitsSet = 0;
        encode(s, true);
    }

  
    public BloomFilter(double c, int n, int k, double falsePositiveProbability) {
        this.expectedNumberOfFilterElements = n;
        this.k = k;
        this.bitsPerElement = c;
        this.bitSetSize = 1200; //(int) Math.ceil(c * n);
        this.fp = falsePositiveProbability;   
        int rho = (int) Math.ceil(- expectedNumberOfFilterElements * Math.log(fp) / Math.pow(Math.log(2),2) );
        numberOfAddedElements = 0;
        this.bitset = new BitSet(bitSetSize);
    }

    
   
    public BloomFilter(String s, double falsePositiveProbability, int expectedNumberOfElements, int grams) {             
        this(Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))) / Math.log(2), // c = k / ln(2)
                expectedNumberOfElements,
                (int) Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))), falsePositiveProbability ); // k = ceil(-log_2(false prob.))
        this.grams = grams;
        this.cols = new int[bitSetSize];
        encode(s, true);
    }

    public double getProbability0() {
        //If we have inserted n elements, the probability that a certain bit is still 0 is
        return Math.pow((1 - 1.0 / this.bitSetSize), this.k * this.numberOfAddedElements);
    }

    public double getProbability1() {
        //the probability that it is 1 is therefore    
        return 1 - getProbability0();
    }

    public void addElement(String s) {
        //word=binascii.a2b_qp(qgram) # convert to binary

        String mykey = "zuxujesw";
        String hex1 = "";
        String hex2 = "";
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(mykey.getBytes(), "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(s.getBytes());
            //System.out.println("size="+digest.length);
            //for (byte b1 : s.getBytes()) {
            //  System.out.println(s+" "+b1);
            //}

            String enc1 = new String(digest);

            for (byte b : digest) {
                //System.out.println(b);
                hex1 = hex1 + String.format("%02x", b);
                //System.out.println(hex1);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            Mac mac = Mac.getInstance("HmacMD5");
            SecretKeySpec secret = new SecretKeySpec(mykey.getBytes(), "HmacMD5");
            mac.init(secret);
            byte[] digest = mac.doFinal(s.getBytes());
            String enc2 = new String(digest);

            //System.out.println(s+" h0="+Hex.encodeHexString(digest));
            for (byte b : digest) {
                hex2 = hex2 + String.format("%02x", b);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // convert hash key to integer
        BigInteger h1 = new BigInteger(hex1, 16);
        BigInteger h2 = new BigInteger(hex2, 16);

        for (int i = 0; i < k; i++) {
            BigInteger bigi = new BigInteger(i + "");
            BigInteger res = h2.multiply(bigi).add(h1).mod(new BigInteger(this.bitSetSize + ""));
            int position = res.intValue();
            if (!bitset.get(position)) {
                bitsSet++;
            }

            
            if (bitset.get(position)) {                
                if (cols[position] == 0) {
                    cols[position] = 1;
                } else {                    
                    cols[position] = cols[position] + 1;
                }
                //System.out.println("collision detected. "+cols[position]);                 
            }

            bitset.set(position);
           // System.out.println(s+" "+position);
        }
        numberOfAddedElements++;
    }

   

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < this.bitSetSize; i++) {
            if (bitset.get(i)) {
                s.append("1");
            } else {
                s.append("0");
            }
        }
        return s.toString();
    }

    public int[] toInt() {
        int[] s = new int[this.bitSetSize];
        for (int i = 0; i < this.bitSetSize; i++) {
            if (bitset.get(i)) {
                s[i] = 1;
            } else {
                s[i] = 0;
            }
        }
        return s;
    }

    public int countZeros() {
        int s = 0;
        for (int i = 0; i < this.bitSetSize; i++) {
            if (!bitset.get(i)) {
                s = s + 1;
            }
        }
        return s;
    }

    public HashSet<Integer> toSet() {
        HashSet<Integer> s = new HashSet();
        for (int i = 0; i < this.bitSetSize; i++) {
            if (bitset.get(i)) {
                s.add(i);
            }
        }
        return s;
    }

    public HashSet<Integer> toSet0() {
        HashSet<Integer> s = new HashSet();
        for (int i = 0; i < this.bitSetSize; i++) {
            if (!bitset.get(i)) {
                s.add(i);
            }
        }
        return s;
    }

    public boolean getBit(int bit) {
        return bitset.get(bit);
    }

    public void setBit(int bit, boolean value) {
        bitset.set(bit, value);
    }

    public BitSet getBitSet() {
        return bitset;
    }

    public int size() {
        return this.bitSetSize;
    }

    public int count() {
        return this.numberOfAddedElements;
    }

    public int sumOfElements() {
        return this.bitsSet;
    }

    public void encode(String s, boolean padded) {
        if (padded) {
            s = "_" + s + "_";
        }
        ArrayList<String> ngrams = NGram.getGrams(s, this.grams);
        for (String gram : ngrams) {
            addElement(gram);
        }
    }

    public void encode(ArrayList<String> numbers, boolean padded) {
        for (String number : numbers) {
            addElement(number);
        }
    }

     public static BitSet toBitSet(String bf) {
        BitSet bs = new BitSet(bf.length());
        for (int i = 0; i < bf.length(); i++) {
            if (bf.charAt(i) == '1') {
                bs.set(i);
            }
        }
        return bs;
    }

    
    
    }
