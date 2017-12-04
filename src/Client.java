import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Client
 * 
 * @author ambarmodi
 */
public class Client {

	public static void main(String[] args) {
		String host=null;
		int port = 0;
		try{ 
			if (args.length != 2){
				throw new Exception();
			}
			host = args[0];
			port = Integer.parseInt(args[1]);
		}catch(Exception ex){
			System.out.println("Incorrect number of arguments! USAGE: ./client.sh <server_ip> <port>");
			System.exit(0);
		}
		
		TTransport transport = new TSocket(host, port);
		try {
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			KeyValueStore.Client client = new KeyValueStore.Client(protocol);
			
			//Request request = new Request();
			//client.put(1, "Sample1", null);
			//client.put(2, "Sample2", null);
			//client.put(1, "Sample3", null);
			Request request = new Request();
			request.setLevel(ConsistencyLevel.ONE);
			request.setIsCoordinator(true);
			if(client.put(1, "string1", request, null)) {
				System.out.println("put is successful");
			} else {
				System.out.println("put is not successful");
			}
			
			transport = new TSocket(host, 9090);
			transport.open();
			protocol = new TBinaryProtocol(transport);
			client = new KeyValueStore.Client(protocol);
			
			System.out.println("retrieved value from server 9090" + client.get(1, request, null));
			//System.out.println(client.get(2, request));
			//System.out.println(client.get(1, request));
			
		} catch (TException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(0);
		}
	}
}
