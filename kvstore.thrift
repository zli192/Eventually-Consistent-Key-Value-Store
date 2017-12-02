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
	1: string timestamp;
	2: ConsistencyLevel level;
}

struct ReplicaID{
	1: string id;
	2: string ip;
	3: i32 port;
}

service KeyValueStore {
  void put(1: Key key,2: Value value, 3: Request request)
    throws (1: SystemException systemException),
  
  Value get(1: Key key, 2: Request request)
    throws (1: SystemException systemException),

  void configureReplicaInfo(1: list<ReplicaID> replica_list),

}