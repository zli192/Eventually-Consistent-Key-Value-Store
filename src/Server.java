import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class Server {

	private static StoreHandler storeHandler;
	private static KeyValueStore.Processor<KeyValueStore.Iface> storeProcessor;
	private static int port;
	private static String ip;

	public static void main(String[] args) {
		String replicaFile = null;
		String replicaName = null;
		try {
			port = Integer.parseInt(args[1]);
			replicaName = args[0];
			ip = InetAddress.getLocalHost().getHostAddress();
			replicaFile = args[2];
		} catch (UnknownHostException e) {
			System.err.println("Error in getting IP: " + e.getMessage());
			System.exit(0);
		} catch (NumberFormatException e) {
			System.err.println("Please specify correct port: " + e.getMessage());
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Error: Incorrect Argument specifies. Usage: ./server.sh <id> <port> <replicas.txt>");
			System.exit(0);
		}

		List<ReplicaID> replList = populateReplicationList(replicaFile);
		try {
			storeHandler = new StoreHandler(replicaName, ip, port, replList);
		} catch (Exception e) {
			System.err.println("Error: Server cannot be initlialize." + e.getMessage());
			System.exit(0);
		}

		storeProcessor = new KeyValueStore.Processor<KeyValueStore.Iface>(storeHandler);

		Runnable simple = new Runnable() {
			public void run() {
				startServer(storeProcessor);
			}
		};
		new Thread(simple).start();
	}

	/**
	 * @param filename containing replicas server list including itself
	 * @return List of Replicas in the system
	 */
	private static List<ReplicaID> populateReplicationList(String filename) {
		List<ReplicaID> replList = new ArrayList<ReplicaID>();
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader br = new BufferedReader(reader);

			String line;
			while ((line = br.readLine()) != null) {
				String[] replica = line.split(" ");
				ReplicaID rep = new ReplicaID();
				rep.id = replica[0];
				rep.ip = replica[1];
				rep.port = Integer.parseInt(replica[2]);
				replList.add(rep);
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error: Invalid input file. Application is terminating.");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replList;

	}

	/**
	 * 
	 * @param processor
	 */
	public static void startServer(KeyValueStore.Processor<KeyValueStore.Iface> processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(port);

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
			System.out.println("************Server details****************");

			System.out.println("\nServer IP: " + ip + "\nServer Port: " + port + "\n");
			server.serve();

		} catch (TTransportException e) {
			System.err.println("Error in opening TTransport socket: " + e.getMessage());
			System.exit(0);
		}
	}
}
