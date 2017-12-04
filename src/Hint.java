
public class Hint {
	
	private ReplicaID replicaID;
	private int key;
	private String value;
	private Request request;

	public Hint(ReplicaID replicaID, int key, String value, Request request) {
		this.replicaID = replicaID;
		this.key = key;
		this.value = value;
		this.request = request;
	}

	public ReplicaID getReplicaID() {
		return replicaID;
	}

	public int getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public Request getRequest() {
		return request;
	}
}