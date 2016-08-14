/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

import gr.eap.LSHDB.util.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Eap_stats {

    HashMap<String, HashMap<String, Integer>> stats = new HashMap<String, HashMap<String, Integer>>();

    public void serialize(Object o) {
        try {
            FileOutputStream fileOut = new FileOutputStream("c:\\voters\\eap\\eap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in c:\\voters\\eap\\eap.ser");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Object deserialize() {
        Object o = null;
        try {
            FileInputStream fileIn = new FileInputStream("c:\\voters\\eap\\eap.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            o = in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException ex1) {
            ex1.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex2) {
            System.out.println("c:\\voters\\eap\\eap.ser not found");
            ex2.printStackTrace();
            return null;
        }
        return o;
    }

    public void stats() {
        for (int k = 2009; k <= 2013; k++) {
            String y = k + "";
            if (stats.containsKey(y)) {
                System.out.println("analyzing year " + y);
                int t = 0;
                HashMap<String, Integer> hm = stats.get(y);
                for (int k1 = 2009; k1 <= 2013; k1++) {
                    String y1 = k1 + "";
                    if (hm.containsKey(y1)) {
                        t = t + hm.get(y1);
                        // System.out.println("  "+y1+"="+hm.get(y1));
                    }
                }
                for (int k1 = 2009; k1 <= 2013; k1++) {
                    String y1 = k1 + "";
                    if (hm.containsKey(y1)) {
                        double percentage = hm.get(y1) * 1.0 / t;
                        System.out.println("  " + y1 + "=" + hm.get(y1) + " " + percentage + " of the total number of duplicates for " + y);
                    }
                }

                System.out.println("  total=" + t + " of " + y);
                //System.out.println("    total duplicates between " + y + " and " + y1 + " = " + total + " " + percentage1 + " of the applicants of " + currentYear);
            }
        }
    }

    public void query() {

        int[] yearB = new int[4];

        String folder = "c:/MAPDB";
        String engine = "gr.eap.LSHDB.MapDB";

        try {

            String file = "c://voters/eap//dataf.csv";
            //String qYear = "2013";

            HashMap<String, Integer> cardinalities = new HashMap<String, Integer>();
            cardinalities.put("2009", 0);
            cardinalities.put("2010", 0);
            cardinalities.put("2011", 0);
            cardinalities.put("2012", 0);
            cardinalities.put("2013", 0);

            int[] t = new int[6];

            HashMap<String, ArrayList<String>> merger = new HashMap<String, ArrayList<String>>();

            //int no = 4;

            /* String[] years = new String[]{"2009", "2010", "2011", "2012", "2013"};
             HammingConfiguration[] hc = new HammingConfiguration[years.length];
             HammingLSHStore[] lsh = new HammingLSHStore[years.length];*/
            //for (int k = 0; k < years.length; k++) {
            /* String dbName = "eap";
            Key key1 = new HammingKey("recordLevel", 30, .1, 110, 1000, false, true);
            HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1}, false);
            hc.saveConfiguration();
            HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc, false);*/
            //}
            String fileName = "c://voters//eap//dataf.csv";

            //FileReader input1 = new FileReader(file);
            //BufferedReader bufRead1 = new BufferedReader(input1);
            BufferedReader bufRead1 = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(new File(fileName)), "UTF8"));

            int lines = FileUtil.countLines(file);

            String currentYear = "";

            int queriesNo = 0;
            int yearCount = 0;
            int totalYearDups = 0;
            for (int i = 0; i < lines; i++) {

                String line1 = bufRead1.readLine();
                StringTokenizer st1 = new StringTokenizer(line1, ",");

                String year = st1.nextToken();

                currentYear = year;


                /*  if (cardinalities.containsKey(year)) {
                 int c1 = cardinalities.get(year);
                 c1++;
                 cardinalities.put(year, c1);
                 }*/
                //if (year.equals(qYear)) {
                queriesNo++;
                yearCount++;
                String afm = st1.nextToken();
                String lastName = st1.nextToken();
                String firstName = st1.nextToken();
                String fatherName = st1.nextToken();
                String street = st1.nextToken();
                String no = st1.nextToken();
                String town = "";
                if (st1.hasMoreTokens()) {
                    town = st1.nextToken();
                }
                String prog = "";
                if (st1.hasMoreTokens()) {
                    prog = st1.nextToken();
                }
                String address = street + " " + no + " " + town;
                String id = year + "_" + i;
                String id1 = "";

                /*
                QueryRecord rec = new QueryRecord(100);

                rec.setId(id);
                rec.set("First Name", firstName);
                rec.set("Last Name", lastName);
                rec.set("Father Name", fatherName);
                rec.set("Address", street + " " + no);
                //rec.set("Programme", prog);                    
                rec.set("SSN", afm);

                rec.set("Year", year);
                //rec.set("Town", town);
                rec.set(1.0, true);*/
                //System.out.println(i);
                //System.out.println("querying "+firstName+" "+lastName+" "+fatherName);
                String s = firstName + "_" + lastName + "_" + fatherName + "_" + prog + "_" + year + "_" + address;
                if (!merger.containsKey(afm)) {

                    /*
                    Result result = lsh.query(rec);
                    result.prepare();
                    if (result != null) {
                        
                       
                        if (result.getRecords().size() > 0) {
                            if (!id1.equals(id)) {
                                //System.out.println("Found duplicates for "+rec.get("First Name")+" "+rec.get("Last Name")+" "+rec.get("Father Name")+" "+rec.get("SSN"));   
                                id1 = id;
                            }
                        }
                        
                        if (result.getRecords().size() > 0) {
                            int times = result.getRecords().size();
                            if (times > 5) {
                                for (int j = 0; j < result.getRecords().size(); j++) {
                                    Record record = result.getRecords().get(j);
                                    //System.out.println("___________" +record.get("Year")+" "+record.get("First Name")+" "+record.get("Last Name")+" "+record.get("Father Name")+" "+record.get("SSN") );
                                }
                                times = 5;
                            }
                            t[times]++;
                        }

                        for (int j = 0; j < result.getRecords().size(); j++) {
                            Record record = result.getRecords().get(j);
                            //System.out.println("___________" +record.get("Year")+" "+record.get("First Name")+" "+record.get("Last Name")+" "+record.get("Father Name")+" "+record.get("SSN") );
                            String data = record.get("First Name") + "_" + record.get("Last Name") + "_" + record.get("Father Name") + "__" + record.get("Programme") + "_" + record.get("Year");
                            arr.add(data);
                            //System.out.println("     "+data);
                            String year1 = (String) record.get("Year");
                            int c2 = stats.get(year1);
                            c2++;
                            stats.put(year1, c2);
                            totalYearDups++;
                        }

                    }*/
                    // }
                    // Add details of the current year as well.
                    //String data = firstName + "_" + lastName + "_" + fatherName + "__" + prog + "_" + year;
                    //arr.add(data);
                    //System.out.println(queriesNo + ". " + s);
                    ArrayList<String> arr = new ArrayList<String>();
                    arr.add(s);
                    merger.put(afm, arr);
                } else {
                    ArrayList<String> arr = merger.get(afm);
                    arr.add(s);
                    merger.put(afm, arr);
                }

            }

            serialize(merger);

            bufRead1.close();

            //for (int k = 0; k < years.length; k++) {
            //lsh.close();
            //}
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }

    }

    public void show(String entry, ArrayList<String> arr) {
        System.out.println(entry);
        for (int i = 0; i < arr.size(); i++) {
            String rec = arr.get(i);
            //System.out.println(rec);  
            String[] v = rec.split("_");
            String afm = v[0];
            String name = v[1] + " " + v[2];
            String fatherName = v[3];
            String prog = v[4];
            String year = v[5];
            System.out.println("          " + afm + " " + name + " " + " " + fatherName + " " + prog + " " + year);
        }

    }

    public String getYear(String[] v) {
        String name = v[0] + " " + v[1];
        String fatherName = v[2];
        String prog = v[3];
        String year = v[4];
        return year;
    }

    public void analyze() {
        int c1 = 0;
        int c2 = 0;
        stats.put("2009", new HashMap<String, Integer>());
        stats.put("2010", new HashMap<String, Integer>());
        stats.put("2011", new HashMap<String, Integer>());
        stats.put("2012", new HashMap<String, Integer>());
        stats.put("2013", new HashMap<String, Integer>());

        int[] times = new int[5 + 1];
        HashMap<String, ArrayList<String>> merger = (HashMap<String, ArrayList<String>>) deserialize();
        System.out.println("size=" + merger.size());
        for (Map.Entry<String, ArrayList<String>> entry : merger.entrySet()) {
            //System.out.println(entry.getKey() + "=" + entry.getValue());
            ArrayList<String> arr = merger.get(entry.getKey());

            HashMap<String, Integer> ded = new HashMap<String, Integer>();
            for (int i = 0; i < arr.size(); i++) {
                String[] v = arr.get(i).split("_");
                String name = v[0] + " " + v[1];
                String fatherName = v[2];
                String prog = v[3];
                String year = v[4];
                if (ded.containsKey(year))
                    arr.remove(i);
                else 
                   ded.put(year, 1);
            }
            times[ded.size()]++;

            for (int i = 0; i < arr.size(); i++) {
                String[] v = arr.get(i).split("_");
                String name = v[0] + " " + v[1];
                String fatherName = v[2];
                String prog = v[3];
                String year = v[4];

                if (stats.containsKey(year)) {

                    HashMap<String, Integer> hm = stats.get(year);
                    for (int k = 0; k < arr.size(); k++) {
                        String[] v1 = arr.get(k).split("_");
                        String year1 = getYear(v1);
                        if (!year1.equals(year)) {
                            if (hm.containsKey(year1)) {
                                int c3 = hm.get(year1);
                                c3++;
                                hm.put(year1, c3);
                                stats.put(year, hm);
                            } else {
                                hm.put(year1, 1);
                                stats.put(year, hm);
                            }
                        }
                    }
                }

                c2++;
                // System.out.println(c2 + "    DUPLICATE!!!!!!!");
                // show(entry.getKey(), arr);                    
            }

        }
        stats();
        for (int i = 1; i < times.length; i++) {
            double percentage = times[i] * 1.0 / merger.size();
            System.out.println(i + " " + times[i] + " " + percentage);
        }

    }

    public static void main(String[] args) {
        Eap_stats pm = new Eap_stats();
        //pm.query();
        pm.analyze();

    }
}
