import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class StoreHandler implements KeyValueStore.Iface {
	private String id;
	private int port;
	private String ip;
	private List<ReplicaID> replicaList;

	/* Constants */
	private static final String logFile = "store.log";
	private static final String DELIMITER = ",";
	private Map<Integer, Value> store;
	private Map<String, List<Hint>> hints;

	/**
	 * Constructor
	 */
	public StoreHandler(String id, String ip, int port, List<ReplicaID> replList) throws NumberFormatException, IOException {
		this.id = id;
		this.port = port;
		this.ip = ip;
		this.replicaList = replList;
		store = new ConcurrentHashMap<Integer, Value>();
		hints = new ConcurrentHashMap<String, List<Hint>>();
		populateStore();
	}

	private void populateStore() throws NumberFormatException, IOException {
		File log = new File(logFile);
		if (log.exists() && !log.isDirectory()) {
			FileReader reader = new FileReader(log);
			BufferedReader br = new BufferedReader(reader);

			String line;
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(DELIMITER, 3);
				store.put(Integer.parseInt(entry[0]), new Value(entry[2], Timestamp.valueOf(entry[1])));
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
	public boolean put(int key, String value, Request request, ReplicaID replicaID) throws SystemException, TException {
//		System.out.println("Trying to put " + key);
		Timestamp timestamp;
		try {
			Value oldValue = store.get(key);
			if(request.isSetTimestamp()) {
				timestamp = Timestamp.valueOf(request.getTimestamp());
			} else {
				timestamp = new Timestamp(System.currentTimeMillis());
			}
			writeToLog(key, new Value(value, timestamp));
			if(request.isIsCoordinator()) {
				boolean isSuccessfull = sendRequestToAllReplicas(key, value, request.setIsCoordinator(false)
						.setTimestamp(timestamp.toString()));
				if(!isSuccessfull) {
					writeToLog(key, oldValue);
					store.put(key, oldValue);
				}
				return isSuccessfull;
			}
		} catch (IOException e) {
			throw new SystemException();
		}
		
		Value valueWithTimestamp = new Value(value, timestamp);
		store.put(key, valueWithTimestamp);
		System.out.println("Value written at server " + port);
		return true;
	}

	private boolean sendRequestToAllReplicas(int key, String value, Request request) {
		int count, i = 0;
		boolean writeSuccessful = false;
		if(request.getLevel() == ConsistencyLevel.ONE) {
			count = 1;
		} else {
			count = 2;
		}
		for(ReplicaID replicaID : replicaList) {
			try {
				TTransport tTransport = new TSocket(replicaID.getIp(), replicaID.getPort());
				tTransport.open();
				TProtocol tProtocol = new TBinaryProtocol(tTransport);
				KeyValueStore.Client client = new KeyValueStore.Client(tProtocol);
				if(client.put(key, value, request, new ReplicaID().setIp(ip).setPort(port).setId(id))) {
					i += 1;
				}
				if(i >= count) {
					writeSuccessful = true;
				}
				tTransport.close();
			} catch (TTransportException e) {
				System.out.println("could not connect to server " + replicaID.getPort());
				storeHintsLocally(replicaID, key, value, request);
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		return writeSuccessful;
	}

	private void storeHintsLocally(ReplicaID replicaID, int key, String value, Request request) {
		System.out.println("store hints locally called");
		Hint hint = new Hint(replicaID, key, value, request);
		if(hints.containsKey(replicaID.getId())) {
			List<Hint> list = hints.get(replicaID.getId());
			list.add(hint);
			hints.put(replicaID.getId(), list);
		} else {
			hints.put(replicaID.getId(), new ArrayList<Hint>());
		}
	}

	private void writeToLog(int key, Value value) throws IOException {
		//TODO: Instead of opening and closing BW each time check if can handle once
		BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
		String logLine = key + DELIMITER + value.getTimestamp() + DELIMITER + value.getValue();
		bw.write(logLine);
		bw.newLine();
		bw.close();
	}

	@Override
	public String get(int key, Request request, ReplicaID replicaID) throws SystemException, TException {
		System.out.println("Trying to get " + key);
		
		return store.get(key).getValue();
	}

}
