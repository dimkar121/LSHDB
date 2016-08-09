# LSHDB
LSHDB is a persistent data engine, which relies on the [Locality-Sensitive Hashing](https://en.wikipedia.org/wiki/Locality-sensitive_hashing) (LSH) technique and noSQL stores, 
for performing [record linkage](https://en.wikipedia.org/wiki/Record_linkage) (including [privacy-preserving record linkage](https://www.cs.cmu.edu/~rjhall/linkage_survey_final.pdf) - PPRL) and similarity search tasks.

The main features of LSHDB are:
* _Easy extensibility_  Support for any noSQL data store, or any LSH technique can be easily plugged by extending or implementing the respective abstract classes or interfaces.
* _Support of both the online query-driven mode and the offline batch process of record linkage_  LSHDB works in two modes; the first mode allows the resolution of the submitted queries in real time, while the second mode works in the traditional offline mode, which reports the results after the record linkage task has been completed.
* _Suport of the PPRL mode_  In the case of PPRL, each participating party, termed also as a data custodian, may send its records, which have been previously masked, to a Trusted Third Party (TTP). The TTP configures and uses LSHDB for performing the linkage task and eventually sending the results back to the respective data custodians.
* _Ease of use_  Queries can be submitted against a data store using just four lines of code.
* _Similarity sliding_  The developer can specify the desired level of similarity between the query and the returned values by using the similarity sliding feature. 
* _Polymorphism of the response_  The result set can be returned either in terms of Java objects, or in JSON \cite{json} format for interoperability purposes.
* _Support of distributed queries_  A query can be forwarded to multiple instances of LSHDB to support data stores that have been horizontally partitioned into multiple compute nodes.

The dependency info for downloading the jar from the central maven repo is:
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
String dbName = "dblp";
String engine = "gr.eap.LSHDB.MapDB";
HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine);
```
one opens a database named `dblp`, which is stored under `/home/LSHDB/stores`, and is created using `Hamming LSH` and `MapDB` (http://www.mapdb.org) as the underlying LSH implementation and noSQL engine, respectively.

In the following, we will showcase how one can (a) insert some records, and (b) submit similarity queries to the `dblp` store.

a) Inserting records to the `dblp` store
Assume a store that contains the titles of the publications contained in [DBLP](http://dblp.uni-trier.de/) along with the name of their first author. In order to support queries with respect to these names, we have to specify a keyed field, from which specialized data structures will be constructed and persisted. If one also needs the title of the publication, then he/she should simply add an additional keyed field.
```
Key key1 = new HammingKey("author");
HammingConfiguration hc = new HammingConfiguration(folder, dbName, engine, new Key[]{key1}, true);
hc.saveConfiguration();
HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, hc, true);
```

b) Query the store
Thw way to submit similarity queries against a store, using keyed fields, is as follows:
```
QueryRecord query = new QueryRecord(dbName, 40); // 40 denotes the max number of the returned records.
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



In case one needs to run LSHDB as a server instance, then, should provide `config.xml` with the following minimum configuration:
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
The above snippet fires up a LSHDB instance, which hosts a single store, on all network interfaces of the local machine listening on port 4443.


For the interested reader, a research paper that deals with three LSH families in conjunction with PPRL is [An LSH-Based Blocking Approach with a Homomorphic Matching Technique for Privacy-Preserving Record Linkage](http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6880802&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D6880802), published by IEEE TKDE (Volume:27, Issue: 4, 2015).
