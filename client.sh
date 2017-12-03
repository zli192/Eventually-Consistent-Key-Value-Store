#!/bin/bash +vx
LIB_PATH=$"/home/abhishek/thrift-0.10.0/lib/java/build/libthrift-0.10.0.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-log4j12-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-api-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/log4j-1.2.17.jar:/home/abhishek/jdk/jdk1.8.0_131/jre/lib/rt.jar"
#simple/secure ip port
java -classpath bin/client_classes:$LIB_PATH Client $1 $2
