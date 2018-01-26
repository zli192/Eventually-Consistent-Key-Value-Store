# Implementation of eventually consistent key-value store that borrows designs from DynamoDB and Cassandra

Note: This project uses Apache Thrift software framework for RPC communication. Refer https://thrift.apache.org/ for details.

-----------------------------------------------------------------------
#### Contributors : Ambar Modi and Abhishek Deshmukh
-----------------------------------------------------------------------

## Overview:
The distributed key-value store includes four replicas. Each replica server is pre-configured with information about all other replicas. The replication factor in this project is 4 i.e. every key-value pair is stored on all four replicas.
Every client's request (get or put) is handled by a coordinator. Client selects any random replica server as the coordinator. 

-----------------------------------------------------------------------
### Consistency level:
Similar to Cassandra, consistency level is configured by the client. When issuing a request, put or get, the client explicitly specifies the desired consistency level: ONE or TWO. For example, receiving a write request with consistency level TWO, the coordinator sends the request to all replicas (including itself). It will respond successful to the client once the write has been written to two replicas. For a read request with consistency level TWO, the coordinator will return the most recent data from two replicas.
With eventual consistency, different replicas may be inconsistent. For example, due to failure, a replica misses one write for a key k. When it recovers, it replays its log to restore its memory state. When a read request for key k comes next, it returns its own version of the value, which is inconsistent. To ensure that all replicas eventually become consistent, the following two procedures are implemented, and your key-value store is configured at the begining to use either of the two.
### Read repair: 
When handling read requests, the coordinator contacts all replicas. If it finds inconsistent data, it will perform “read repair” in the background.
### Hinted handoff:
During write, the coordinator tries to write to all replicas. As long as enough replicas have succeeded, ONE or TWO, it will respond successful to the client. However, if not all replicas succeeded, e.g., three have succeeded but one replica server has failed, the coordinator would store a “hint” locally. If at a later time the failed server has recovered, it might be selected as coordinator for another client’s request. This will allow other
replica servers that have stored “hints” for it to know it has recovered and send over the stored hints.
If not enough replicas are available, e.g., consistency level is configured to TWO, but only one replica is available, then the coordinator should return an exception to the issuing client.

-----------------------------------------------------------------------
#### Client:
Client issues a stream of get and put requests to the key-value store. Once started, the client acts as a console, allowing users to issue a stream of requests. The client selects a random replica server as the coordinator for all its requests. That is, all requests from a single client are handled by the same coordinator.
There can be multiple clients, potentially issue requests to different coordinators at the same time.

-----------------------------------------------------------------------
## Instructions to execute.
1. thrift -gen java kvstore.thrift			(To generate the chord classes. It will be generated in gen-java)
2. make 									(This will compile the program)
3. ./server.sh <id> <port> <replicas.txt> <true/false> (true for hintedhandoff/false for readrepair: This will start the server on the mentioned port)
4. ./client.sh <replicas.txt>				(To hit on a server on specified port and ip)
5. make clean 								(Optional : This will clean compiled .class files)
6. Ctrl + C 								(To Terminate/kill the server)roject's README

-----------------------------------------------------------------------
## Note:
1. Input file replica.txt should be in below format(ReplicaID Replicas-IP PORT):
eg:
rep1 128.226.180.165 9001
rep2 128.226.180.165 9002
rep3 128.226.180.165 9003
rep4 128.226.180.164 9004

2. Need to provide thrift path before step1. i.e. export PATH=$PATH:/home/ambarm/src_code/local/bin
-----------------------------------------------------------------------
