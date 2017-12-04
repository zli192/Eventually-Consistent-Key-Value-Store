LIB_PATH=/home/abhishek/thrift-0.10.0/lib/java/build/libthrift-0.10.0.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-log4j12-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/slf4j-api-1.7.12.jar:/home/abhishek/thrift-0.10.0/lib/java/build/lib/log4j-1.2.17.jar:/home/abhishek/jdk/jdk1.8.0_131/jre/lib/rt.jar
all: clean
	mkdir bin
	mkdir bin/client_classes
	mkdir bin/server_classes
	javac -classpath $(LIB_PATH) -d bin/client_classes/ src/Client.java src/StoreHandler.java src/Value.java gen-java/*
	javac -classpath $(LIB_PATH) -d bin/server_classes/ src/Server.java src/StoreHandler.java src/Value.java gen-java/*


clean:
	rm -rf bin/


