#!/bin/bash +vx
LIB_PATH=$"/home/abhishek/thrift-0.10.0/lib/java/build/libthrift-0.10.0.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-log4j12-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-api-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/log4j-1.2.17.jar:/home/abhishek/jdk/jdk1.8.0_131/jre/lib/rt.jar"
#port
java -classpath bin/server_classes:$LIB_PATH Server $1 $2 $3 $4
