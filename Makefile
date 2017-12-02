LIB_PATH=/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/libthrift-0.10.0.jar:/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/slf4j-log4j12-1.7.12.jar:/Users/ambarmodi/Documents/java/workspace/amodi1-p2/lib/slf4j-api-1.7.12.jar
all: clean
	mkdir bin
	mkdir bin/client_classes
	mkdir bin/server_classes
	javac -classpath $(LIB_PATH) -d bin/client_classes/ src/Client.java src/StoreHandler.java gen-java/*
	javac -classpath $(LIB_PATH) -d bin/server_classes/ src/Server.java src/StoreHandler.java gen-java/*


clean:
	rm -rf bin/

