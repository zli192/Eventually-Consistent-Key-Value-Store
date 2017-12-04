typedef string Value
typedef i32 Key

exception SystemException {
  1: optional string message
}

enum ConsistencyLevel {
	ONE;
	TWO;
}

struct Request {
	1: optional string timestamp;
	2: required ConsistencyLevel level;
	3: required bool isCoordinator;
}

struct ReplicaID{
	1: string id;
	2: string ip;
	3: i32 port;
}

service KeyValueStore {
  bool put(1: Key key,2: Value value, 3: Request request, 4: ReplicaID replicaID)
    throws (1: SystemException systemException),
  
  Value get(1: Key key, 2: Request request, 3: ReplicaID replicaID)
    throws (1: SystemException systemException),


}