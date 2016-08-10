# LSHDB
LSHDB is a persistent data engine, which relies on the [locality-sensitive hashing](https://en.wikipedia.org/wiki/Locality-sensitive_hashing) (LSH) technique and noSQL data stores, 
for performing [record linkage](https://en.wikipedia.org/wiki/Record_linkage) (including [privacy-preserving record linkage](https://www.cs.cmu.edu/~rjhall/linkage_survey_final.pdf) - PPRL) and similarity search tasks.

The main features of LSHDB are:
* __Easy extensibility__  Support for any noSQL data store, or any LSH technique can be easily plugged by extending or implementing the respective abstract classes or interfaces.
* __Support of both the online query-driven mode and the offline batch process of record linkage__  LSHDB works in two modes; the first mode allows the resolution of the submitted queries in real time, while the second mode works in the traditional offline mode, which reports the results after the record linkage task has been completed.
* __Suport of the PPRL mode__  In the case of PPRL, each participating party, termed also as a data custodian, may send its records, which have been previously masked, to a Trusted Third Party (TTP). The TTP configures and uses LSHDB for performing the linkage task and eventually sending the results back to the respective data custodians.
* __Ease of use__  Queries can be submitted against a data store using just four lines of code.
* _Similarity sliding__  The developer can specify the desired level of similarity between the query and the returned values by using the similarity sliding feature. 
* __Polymorphism of the response__  The result set can be returned either in terms of Java objects, or in JSON \cite{json} format for interoperability purposes.
* __Support of distributed queries__  A query can be forwarded to multiple instances of LSHDB to support data stores that have been horizontally partitioned into multiple compute nodes.
* __Support of storing structured and semi-structured data__  Each record of a data store may contain homogeneous or heterogeneous data.


The dependency info for downloading the jar (ver. 1.0) from the central maven repo is:
```
<dependency>
    <groupId>gr.eap.LSHDB</groupId>
    <artifactId>LSHDB</artifactId>
    <version>1.0</version>
</dependency>
```


Stores created by LSHDB can be accessed either in-line or using sockets. 
In the in-line mode, using a simple initialization code snippet of the following form:
```
String folder = "/home/LSHDB/stores";
String storeName = "dblp";
String engine = "gr.eap.LSHDB.MapDB";
HammingLSHStore lsh = new HammingLSHStore(folder, storeName, engine);
```
one opens a database named `dblp`, which is stored under `/home/LSHDB/stores`, and is created using `Hamming LSH` and (`MapDB`)[http://www.mapdb.org] as the underlying LSH implementation and noSQL engine, respectively.


In the following, we will showcase how one can (a) insert some records, and, then, (b) submit similarity queries to the `dblp` store.

a) Inserting records to the `dblp` store
Assume a store that contains the titles of the publications contained in [DBLP](http://dblp.uni-trier.de/) along with the name of their first author. In order to support queries with respect to these names, we have to specify a keyed field, from which specialized data structures will be constructed and persisted. If one also needs to submit queries uisng the titles of the publications, then he/she should simply add an additional keyed field.
```
Key key1 = new HammingKey("author");
Key key2 = new HammingKey("title");
HammingConfiguration hc = new HammingConfiguration(folder, storeName, engine, new Key[]{key1, key2}, true);
hc.saveConfiguration();
HammingLSHStore lsh = new HammingLSHStore(folder, storeName, engine, hc, true);

// iterate the records from a relational db or from a text file
Record record = new Record();
record.setId(id);  // this value should be unique
record.set("author", fullName);
record.set("author"+Key.TOKENS, new String[]{surname}); 
record.set("title", title);
// extract some important keywords from the title
record.set("title"+Key.TOKENS, keywords); // keywords should be a String array
lsh.insert(record);

lsh.close();
```
The object `record` may store any kind of field depending on the running application; a publication may refer to a cenference `record.set("conference", conferenceInfo);` or to a journal `record.set("journal", journalInfo);`.

b) Querying the `dblp` store
Thw way to submit similarity queries against a store, using keyed fields, is as follows:
```
QueryRecord query = new QueryRecord(storeName, 40); // 40 denotes the max number of the returned records.
query.setKeyedField("author", new String[]{"John"},1.0,true);
Result result = lsh.query(query);
result.prepare();  
ArrayList<Record> arr = result.getRecords();
```

Using the above query for the records mentioned above, the results contain the following entries:

- M. R. Stalin __John__ An investigation of ball burnishing process on CNC lathe using finite element analysis
- Christian __John__ Transformation und ontologische Formulierung multikriterieller Problemstellungen 
- Benjamin __Johnen__ A Dynamic Time Warping algorithm for industrial robot 
- Donghee Yvette __Wohn__ Understanding Perceived Social Support through Communication Time  Frequency  and Media Multiplexity
- Colette __Johnen__ Memory Efficient Self-stabilizing Distance-k Independent Dominating Set Construction etc.

By sliding the threshold to the left (tightening) 
`query.setKeyedField("author", new String[]{"John"},.8,true);`
we narrow the reults, which get closer to the query value ("John"):

- Aaron __Johnson__ Computational Objectivity in Depression Assessment for Unstructured Large Datasets
- M. R. Stalin __John__ An investigation of ball burnishing process on CNC lathe using finite element analysis
- Christian __John__ Transformation und ontologische Formulierung multikriterieller Problemstellungen 
- Michael __Johnson__ Unifying Set-Based  Delta-Based and Edit-Based Lenses
- Rachel St. __John__ Spatially explicit forest harvest scheduling with difference equations etc.


In case one needs to run LSHDB as a server instance, then, should provide the following minimum configuration:
```
<server>
   <port>
      4443
   </port>
<stores>
  <store>
     <name>
         dblp
     </name>  
     <folder>
          /home/LSHDB/stores
     </folder>
     <engine>
          gr.eap.LSHDB.MapDB
     </engine>
     <LSHStore>
          gr.eap.LSHDB.HammingLSHStore
      </LSHStore>
      <LSHConfiguration>
          gr.eap.LSHDB.HammingConfiguration
      </LSHConfiguration>	   	   
  </store>
 </stores> 
</server>
```
Save the above snippet as `config.xml` into a folder `someFolder` and then run 
`mvn exec:java  -Dexec.mainClass="gr.eap.LSHDB.Server" -Dexec.args="/someFolder/"`. A LSHDB instance will be fired up, hosting a single store, and listening on all network interfaces of the local machine on port 4443.

The correpsonding client application should specify the server/port through a `client` object, and, in turn, submit the query.
```
Client client = new Client(server, port);
Result result = client.queryServer(query);
```
Note that the `query` object holds the name of the store that will be queried. LSHDB doe not maintain any server-side persistent connections.


For the interested reader, a research paper that deals with three LSH families in conjunction with PPRL is [An LSH-Based Blocking Approach with a Homomorphic Matching Technique for Privacy-Preserving Record Linkage](http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6880802&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D6880802), published by IEEE TKDE (Volume:27, Issue: 4, 2015).
