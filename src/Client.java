import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Client to test the DHT Cord implementation.
 * 
 * @author ambarmodi
 */
public class Client {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Incorrect number of arguments! Client is terminating...");
			System.exit(0);
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		TTransport transport = new TSocket(host, port);
		try {
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			KeyValueStore.Client client = new KeyValueStore.Client(protocol);
			//client.configureReplicaInfo(null);
			
		} catch (TException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(0);
		}
	}
}
