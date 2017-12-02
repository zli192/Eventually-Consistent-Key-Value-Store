import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class Server {

	public static StoreHandler storeHandler;
	public static KeyValueStore.Processor<KeyValueStore.Iface> storeProcessor;
	public static int port;
	public static String ip;

	public static void main(String[] args) {

		try {
			port = Integer.parseInt(args[0]);
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.err.println("Error in getting IP: " + e.getMessage());
			System.exit(0);
		} catch (NumberFormatException e) {
			System.err.println("Please specify correct port: " + e.getMessage());
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Error: Incorrect Argument specifies. Usage: " + e.getMessage());
			System.exit(0);
		}

		storeHandler = new StoreHandler(ip, port);
		storeProcessor = new KeyValueStore.Processor<KeyValueStore.Iface>(storeHandler);

		Runnable simple = new Runnable() {
			public void run() {
				startServer(storeProcessor);
			}
		};
		new Thread(simple).start();
	}

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
