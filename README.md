![LSHDB](https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/Sierpinski_deep.svg/128px-Sierpinski_deep.svg.png)


# LSHDB 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gr.eap.LSHDB/LSHDB/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22gr.eap.LSHDB%22)


LSHDB is a parallel and distributed data engine, which relies on the [locality-sensitive hashing](https://en.wikipedia.org/wiki/Locality-sensitive_hashing) (LSH) technique and noSQL systems, 
for performing [record linkage](https://en.wikipedia.org/wiki/Record_linkage) (including [privacy-preserving record linkage](https://www.cs.cmu.edu/~rjhall/linkage_survey_final.pdf) - PPRL) and similarity search tasks. Parallelism lies at the core of its mechanism, since queries are executed in parallel using a pool of threads.

##The main features of LSHDB are:
* __Easy extensibility__  Support for any noSQL data store, or any LSH technique can be easily plugged by extending or implementing the respective abstract classes or interfaces.
* __Support of both the online query-driven mode and the offline batch process of record linkage__  LSHDB works in two modes; the first mode allows the resolution of the submitted queries in real time, while the second mode works in the traditional offline mode, which reports the results after the record linkage task has been completed.
* __Suport of the PPRL mode__  In the case of PPRL, each participating party, termed also as a data custodian, may send its records, which have been previously masked, to a Trusted Third Party (TTP). The TTP configures and uses LSHDB for performing the linkage task and eventually sending the results back to the respective data custodians.
* __Ease of use__  Queries can be submitted against a data store using just four lines of code.
* __Similarity sliding__  The user can specify the desired level of similarity between the query and the returned values by using the similarity sliding feature. 
* __Polymorphism of the response__  The result set can be returned either in terms of Java objects, or in `JSON` format for interoperability purposes.
* __Support of distributed queries__  A query can be forwarded to multiple instances of LSHDB to support data stores that have been horizontally partitioned into multiple compute nodes.
* __Support of storing structured and semi-structured data__  A data store may contain homogeneous or heterogeneous data.


The dependency info for downloading the jar (ver. 1.0) from the central maven repo is:
```xml
<dependency>
    <groupId>gr.eap.LSHDB</groupId>
    <artifactId>LSHDB</artifactId>
    <version>1.0</version>
</dependency>
```


Stores created by LSHDB can be accessed either in-line or using sockets. 
In the in-line mode, using a simple initialization code snippet of the following form:
```java
String folder = "/home/LSHDB/stores";
String storeName = "dblp";
String engine = "gr.eap.LSHDB.MapDB";
HammingLSHStore lsh = new HammingLSHStore(folder, storeName, engine);
```
one opens a database named `dblp`, which is stored under `/home/LSHDB/stores`, and is created using `Hamming LSH` and `MapDB`[http://www.mapdb.org] as the underlying LSH implementation and noSQL engine, respectively.


In the following, using the  we will showcase how one can insert some records, and submit similarity queries either by using Java objects or by performing asynchronous `AJAX` requests.


##Inserting records into a store

Assume a store that contains the titles of the publications contained in [DBLP](http://dblp.uni-trier.de/) along with the name of their first author. In order to support queries with respect to these names, we have to specify a keyed field, from which specialized data structures will be constructed and persisted. If one also needs to submit queries uisng the titles of the publications, then he/she should simply add an additional keyed field.
```java
Key key1 = new HammingKey("author");
Key key2 = new HammingKey("title");
HammingConfiguration hc = new HammingConfiguration(folder, storeName, engine, new Key[]{key1, key2}, true);
hc.saveConfiguration();
HammingLSHStore lsh = new HammingLSHStore(folder, storeName, engine, hc, true);

// iterate the records from a relational db or from a text file
Record record = new Record();
record.setId(id);  // this value uniquely identifies an author
record.set("author", fullName);
record.set("author"+Key.TOKENS, new String[]{surname}); 
record.set("title", title);
// extract some important keywords from the title
record.set("title"+Key.TOKENS, keywords); // keywords should be a String array
lsh.insert(record);

lsh.close();
```
The object `record` may store any kind of fields depending on the running application; a publication may refer to a cenference `record.set("conference", conferenceInfo);` or to a journal `record.set("journal", journalInfo);`.

##Querying a store

The following snippet submits a similarity query against the `dblp` store, using keyed fields.
```java
QueryRecord query = new QueryRecord(n); // n denotes the max number of the returned records.
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

##Running LSHDB as a server isntance
In case one needs to run LSHDB as a server instance, then, should provide the following minimum configuration:
```xml
<LSHDB>
   <alias>local</alias>
   <port>4443</port>
   <stores>
     <store>
       <name>dblp</name>  
       <folder>/home/LSHDB/stores</folder>
       <engine>gr.eap.LSHDB.MapDB</engine>
       <LSHStore>gr.eap.LSHDB.HammingLSHStore</LSHStore>
       <LSHConfiguration>gr.eap.LSHDB.HammingConfiguration</LSHConfiguration>    
     </store>
   </stores>    
</LSHDB>
```
Save the above snippet as `config.xml` into some folder and then run:

`mvn exec:java  -Dexec.mainClass="gr.eap.LSHDB.Server" -Dexec.args="/someFolder/"`,

which will fire up a LSHDB instance, hosting a single store, and listening on all network interfaces of the local machine on port 4443.

The correpsonding client application should specify the server/port through a `client` object, and, in turn, submit the query.
```java
Client client = new Client(server, port);
Result result = client.queryServer(query);
```
Note that the `query` object holds the name of the store that will be queried. LSHDB does not maintain any server-side persistent connections. 

In all the above listings, the handling of any checked thrown exceptions (such as `StoreInitException`, `ConnectException`, `UnknownHostException` etc.) is omitted for brevity.


##Performing asynchronous `AJAX` requests
Assuming a fully functional instance running on `localhost` at port `4443`, which hosts the `dblp` store, one by submitting the url `http://localhost:4443/JSON/dblp?author_Query=John` through a web browser, receives the results in `JSON` format. A more advanced option is to use `jquery` as follows:
```javascript
    $.ajax({
	url:"http://"+server+":"+port+"/JSON/dblp",
	type:"get",
	data:{author_Query: $('#authorText').val()},
        dataType: 'jsonp', 
        success: function(json) {
			if (json.error){
			        out="Error: "+json.errorMessage;
			} else { 
		                out="<table>"; 	  
			        for(i = 0; i < json.length; i++) {
				        out += "<tr><td>"+(i+1)+".</td><td>" +  json[i].author + "</td><td>" + 
				        json[i].title + "</td><td>" +  json[i].year + "</td></tr>";
			         }
                           	 out += "</table>";
			}
                        document.getElementById("id01").innerHTML = out;
        }
    });
```

##Distributed settings
To showcase the distributed extensions of LSHDB, assume that records of the `dblp` store have been horizontally partitioned to three compute nodes, namely `n1`, `n2`, and `n3`, where `n2` and `n3` have been registered as remote nodes to `n1`. Subsequently, a client may submit a query to `n1`, which forwards that query to `n2` and `n3` in parallel using a pool of threads. Upon completion of the local and remote queries, `n1` sends the results back to the client. The following snippet registers `n2` and `n3` to `n1`.
```xml
<remote_nodes>
      	<remote_node>
		<alias>n2</alias>
		<port>4443</port>
	   	<url>some ip or fqdn</url>
	   	<enabled>true</enabled>
      	</remote_node>
      	<remote_node>
		<alias>n3</alias>
	   	<url>some ip or fqdn</url>
           	<port>4443</port>
	   	<enabled>true</enabled>
       	</remote_node>
</remote_nodes>
```
We also have to denote which of these server aliases support our specified stores. This is achieved by adding the following snippet to the correponding `store` tags.
```xml
<remote_stores> 
       <remote_store>
 	     <alias>n2</alias>
	</remote_store>
	<remote_store>
	     <alias>n3</alias>
	 </remote_store>
</remote_stores>
```


###References
For the interested reader, we suggest the following research papers:
* [Similarity Search in High Dimensions via Hashing](http://www.vldb.org/conf/1999/P49.pdf), presented in VLDB (1999).
* [A fast and efficient Hamming LSH-based scheme for accurate linkage](http://link.springer.com/article/10.1007/s10115-016-0919-y), published by KAIS (2016).
* [Efficient Record Linkage Using a Compact Hamming Space](https://openproceedings.org/2016/conf/edbt/paper-56.pdf), presented in EDBT (2016).
* [An LSH-Based Blocking Approach with a Homomorphic Matching Technique for Privacy-Preserving Record Linkage](http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6880802&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D6880802), published by IEEE TKDE (Volume:27, Issue: 4, 2015).
 

![LSHDB](https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/Sierpinski_deep.svg/128px-Sierpinski_deep.svg.png)
######Image owned by Sega sai [CC BY-SA 3.0 (http://creativecommons.org/licenses/by-sa/3.0) or GFDL (http://www.gnu.org/copyleft/fdl.html)], via Wikimedia Commons **
