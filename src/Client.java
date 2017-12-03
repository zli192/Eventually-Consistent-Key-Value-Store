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
			
			System.out.println(client.get(1, null));
			System.out.println(client.get(2, null));
			System.out.println(client.get(1, null));
			
		} catch (TException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(0);
		}
	}
}
