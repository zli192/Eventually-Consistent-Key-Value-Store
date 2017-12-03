import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
		System.out.println("Trying to put "+key);
		try {
			writeToLog(key,value);
		} catch (IOException e) {
			throw new SystemException();
		}
		store.put(key, value);
		
	}

	private void writeToLog(int key, String value) throws IOException {
		//TODO: Instead of opening and closing BW each time check if can handle once
		BufferedWriter bw = new BufferedWriter(new FileWriter(logFile,true));
		String logLine= key+DELIMITER+value;
		bw.write(logLine);
		bw.newLine();
		bw.close();
	}

	@Override
	public String get(int key, Request request) throws SystemException, TException {
		System.out.println("Trying to get "+key);
		
		return store.get(key);
	}

	@Override
	public void configureReplicaInfo(List<ReplicaID> replica_list) throws TException {

	}

}
