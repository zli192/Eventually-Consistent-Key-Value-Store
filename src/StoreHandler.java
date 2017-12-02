import java.util.List;

import org.apache.thrift.TException;

public class StoreHandler implements KeyValueStore.Iface{
	public int port;
	public String ip;

	private List<ReplicaID> replicaList;
	
	public StoreHandler(String ip, int port) {
		this.port = port;
		this.ip = ip;	
	}

	@Override
	public void put(int key, String value, Request request) throws SystemException, TException {
		System.out.println("Put has been called");
	}

	@Override
	public String get(int key, Request request) throws SystemException, TException {
		System.out.println("Get has been called");
		return null;
	}

	@Override
	public void configureReplicaInfo(List<ReplicaID> replica_list) throws TException {
		System.out.println("configureReplicaInfo called");
	}

}
