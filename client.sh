#!/bin/bash +vx
LIB_PATH=$"/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/libthrift-0.10.0.jar:/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/slf4j-log4j12-1.7.12.jar:/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/slf4j-api-1.7.12.jar:/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/log4j-1.2.17.jar"
#simple/secure ip port
java -classpath bin/client_classes:$LIB_PATH Client $1 $2
