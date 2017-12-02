
## Instructions to execute.
1. thrift -gen java kvstore.thrift		(To generate the chord classes. It will be generated in gen-java)
2. make 								(This will compile the program)
3. ./server.sh <port_no>				(This will start the server on the mentioned port)
4. ./client.sh <ip> <port>				(To hit on a server on specified port and ip)
5. make clean 							(Optional : This will clean compiled .class files)
6. Ctrl + C 							(To Terminate/kill the server)roject's README
