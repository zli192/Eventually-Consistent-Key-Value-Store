import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;

public class StoreHandler implements KeyValueStore.Iface {
	private int port;
	private String ip;
	private List<ReplicaID> replicaList;

	/* Constants */
	private static final String logFile = "store.log";
	private static final String DELIMITER = ":";
	private Map<Integer, String> store;

	/**
	 * Constructor
	 */
	public StoreHandler(String ip, int port, List<ReplicaID> replList) throws NumberFormatException, IOException {
		this.port = port;
		this.ip = ip;
		this.replicaList = replList;
		store = new ConcurrentHashMap<Integer, String>();
		populateStore();
	}

	private void populateStore() throws NumberFormatException, IOException {
		File log = new File(logFile);
		if (log.exists() && !log.isDirectory()) {
			FileReader reader = new FileReader(log);
			BufferedReader br = new BufferedReader(reader);

			String line;
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(DELIMITER);
				store.put(Integer.parseInt(entry[0]), entry[1]);
			}
			br.close();
		} else {
			if(!(log.createNewFile())){
				System.err.println("ERROR: File cannot be created.");
				System.exit(0);
			}
		}

	}

	@Override
	public void put(int key, String value, Request request) throws SystemException, TException {
		// First write to a log File
		System.out.println("Put has been called");
	}

	@Override
	public String get(int key, Request request) throws SystemException, TException {
		System.out.println("Get has been called");
		for (ReplicaID r : replicaList) {
			System.out.println(r.id);
		}
		return null;
	}

	@Override
	public void configureReplicaInfo(List<ReplicaID> replica_list) throws TException {

	}

}
