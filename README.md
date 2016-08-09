# LSHDB
LSHDB is a persistent data engine, which relies on Locality-Sensitive Hashing and noSQL stores, 
for performing record linkage (and privacy-preserving record linkage) and similarity search tasks.

Stores created by LSHDB can be accessed either in-line or using sockets. 
In the in-line mode, using a simple initialization code snippet of the following form:
```
String folder = "/home/LSHDB/stores";
String dbName = "dblp";
String engine = "gr.eap.LSHDB.MapDB";
HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, null,true);
```
one opens a database named `dblp`, which is stored under `/home/LSHDB/stores`, and is created using `Hamming LSH` and `MapDB` (http://www.mapdb.org) as the underlying LSH implemntation and noSQL engine, repsectively.

Preliminary details abput LSH can be found at [https://en.wikipedia.org/wiki/Locality-sensitive_hashing]. 

In case one needs to run LSHDB as a server instance, then, should provide `config.xml` with the following minimum configuaration:
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


