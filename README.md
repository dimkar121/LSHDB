# LSHDB
LSHDB is a persistent data engine, which relies on Locality-Sensitive Hashing and noSQL stores, 
for performing record linkage (and privacy-preserving record linkage) and similarity search tasks.

Stores created by LSHDB can be accessed either in-line or using sockets. 
In the in-line mode, using a simple initialization code snippet of the following form:
```
String folder = "c:/MAPDB";
String dbName = "dblp";
String engine = "gr.eap.LSHDB.MapDB";
HammingLSHStore lsh = new HammingLSHStore(folder, dbName, engine, null,true);
```
one opens a database named `dblp`, which is stored under `c:/MAPDB`, and is created using `Hamming LSH` and `MapDB` as the underlying LSH implemntation and noSQL engine, repsectively.

Preliminary details abput LSH can be found at [https://en.wikipedia.org/wiki/Locality-sensitive_hashing]. 
