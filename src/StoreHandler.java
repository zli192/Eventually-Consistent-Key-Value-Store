import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	
	private boolean useReadRepair;
	private boolean useHintedHandoff;

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
		useReadRepair = true;
		useHintedHandoff = false;
		populateStore();
	}

	private void populateStore() throws NumberFormatException, IOException {
		File log = new File(id + logFile);
		if (log.exists() && !log.isDirectory()) {
			FileReader reader = new FileReader(log);
			BufferedReader br = new BufferedReader(reader);

			String line;
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(DELIMITER, 3);
				System.out.println("retrieved from log " + entry[0] + "," + entry[2] + "," + entry[1]);
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
		Value oldValue = store.get(key);
		try {
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
		if(hints.containsKey(replicaID.getId())) {
			performHintedHandoff(replicaID);
		}
		
		if(oldValue == null || oldValue.getTimestamp().before(timestamp)) {
			System.out.println("The put request is the most recent");
			Value valueWithTimestamp = new Value(value, timestamp);
			store.put(key, valueWithTimestamp);
			System.out.println(replicaID.getId() + " wrote value " + value + " at replica " + id + " for key " + key);
		}
		return true;
	}

	private void performHintedHandoff(ReplicaID replicaID) {
		System.out.println("Hinted handoff being performed on replica " + replicaID.getId() + " by replica " + id);
		List<Hint> listOfHints = hints.get(replicaID.getId());
		for(Hint hint : listOfHints) {
			TTransport tTransport = new TSocket(replicaID.getIp(), replicaID.getPort());
			try {
				tTransport.open();
				TProtocol tProtocol = new TBinaryProtocol(tTransport);
				KeyValueStore.Client client = new KeyValueStore.Client(tProtocol);
				client.put(hint.getKey(), hint.getValue(), hint.getRequest(), 
						new ReplicaID().setIp(ip).setPort(port).setId(id));
				tTransport.close();
			} catch (TTransportException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		hints.remove(replicaID.getId()); 
		
		
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
		List<Hint> list;
		if(hints.containsKey(replicaID.getId())) {
			list = hints.get(replicaID.getId());
		} else {
			list = new ArrayList<Hint>();
		}
		list.add(hint);
		hints.put(replicaID.getId(), list);
	}

	private void writeToLog(int key, Value value) throws IOException {
		//TODO: Instead of opening and closing BW each time check if can handle once
		BufferedWriter bw = new BufferedWriter(new FileWriter(id + logFile, true));
		String logLine = key + DELIMITER + value.getTimestamp() + DELIMITER + value.getValue();
		bw.write(logLine);
		bw.newLine();
		bw.close();
	}


	@Override
	public String get(int key, Request request, ReplicaID replicaID) throws SystemException, TException {
		String returnValue = null;
		if (useReadRepair) {
			System.out.println("Trying to get " + key);
			if (request.isIsCoordinator()) {
				returnValue = readFromAllReplicas(key, request);
			} else {
				returnValue = store.get(key).getTimestamp() + DELIMITER + store.get(key).getValue();
			}
			//return value;
		}
		if (useHintedHandoff) {
			System.out.println("Trying to get " + key);
			if (hints.containsKey(replicaID.getId())) {
				performHintedHandoff(replicaID);
			}
			if (store.get(key) == null) {
				System.out.println("the value for key " + key + " is null");
				returnValue = new String();
			}
			returnValue = store.get(key).getValue();
		}
		
		return returnValue;
	}

	/**
	 * Read data from all Replicas.And if configured start the read repair
	 */
	private String readFromAllReplicas(int key, Request request) throws SystemException, TException {
		int consistencyLevel;
		List<Value> valueList = new ArrayList<Value>();
		Map<ReplicaID,Value> readRepairList = new HashMap<ReplicaID,Value>();		

		String result = null;

		if (request.getLevel() == ConsistencyLevel.ONE) {
			consistencyLevel = 1;
		} else {
			consistencyLevel = 2;
		}
		valueList.add(store.get(key));

		for (ReplicaID replicaID : replicaList) {
			
			if (valueList.size() >= consistencyLevel) {
				result = getUpdatedValue(valueList);
			}
			if (replicaID.getId().equals(id)) {
				// Don't read from itself
				readRepairList.put(replicaID, store.get(key));
				continue;
			}
			
			TTransport tTransport = new TSocket(replicaID.getIp(), replicaID.getPort());
			tTransport.open();
			TProtocol tProtocol = new TBinaryProtocol(tTransport);
			KeyValueStore.Client client = new KeyValueStore.Client(tProtocol);

			String val = client.get(key, request.setIsCoordinator(false), replicaID);
			String[] tsVal = val.split(DELIMITER, 2);
			Value value = new Value(tsVal[1], java.sql.Timestamp.valueOf(tsVal[0]));
			valueList.add(value);
			readRepairList.put(replicaID, value);

			tTransport.close();

		}
		//Start this method in background.
		Runnable runnable = new Runnable() {		
			@Override
			public void run() {
				try {
					startReadRepair(key,request, readRepairList);
				} catch (TException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		(new Thread(runnable)).start();
		return result;
	}

	/**
	 * Starts the readRepair mechanism 
	 */
	private void startReadRepair(int key, Request request, Map<ReplicaID, Value> readRepairList) throws SystemException, TException {
		System.out.println("Read Repair started");
		Value newestValue=null;
		for(Value value : readRepairList.values()){
			if(newestValue==null){
				newestValue=value;
			} else{
				if(newestValue.getTimestamp().compareTo(value.getTimestamp()) < 0){
					newestValue = value;
				}
			}
		}
		
		//Now we have newest value.. we can override values on each replica here.
		for(Entry<ReplicaID, Value> entry : readRepairList.entrySet()){
			ReplicaID replica = entry.getKey();
			Value value = entry.getValue();
			if(newestValue.getTimestamp().compareTo(value.getTimestamp()) > 0){
				//Update value on that replica.
				TTransport tTransport = new TSocket(replica.getIp(), replica.getPort());
				tTransport.open();
				TProtocol tProtocol = new TBinaryProtocol(tTransport);
				KeyValueStore.Client client = new KeyValueStore.Client(tProtocol);
				client.put(key, value.getValue(), request, new ReplicaID().setIp(ip).setPort(port).setId(id));
				tTransport.close();
			}
		}
	}

	/**
	 * Get the recent value from value list.
	 * @param valueList
	 * @return
	 */
	private String getUpdatedValue(List<Value> valueList) {
		Value newestValue = valueList.get(0);
		for(Value value : valueList){
			if(newestValue.getTimestamp().compareTo(value.getTimestamp()) < 0){
				newestValue = value;
			}
		}
		return newestValue.getTimestamp()+DELIMITER+newestValue.getValue();
	}
	
}
