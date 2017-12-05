import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		String replicasFileName = null;
		List<ReplicaID> replList = new ArrayList<ReplicaID>();
		ReplicaID coordinator;
		try{ 
			if (args.length != 1){
				throw new Exception();
			}
			replicasFileName = args[0];
			FileReader reader = new FileReader(replicasFileName);
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
		} catch(Exception ex){
			System.out.println("Incorrect number of arguments! USAGE: ./client.sh replicas.txt");
			System.exit(0);
		}
		Random rand = new Random();
		coordinator = replList.get(rand.nextInt(3) + 0);
		
		TTransport transport = new TSocket(coordinator.getIp(), coordinator.getPort());
		System.out.println("Connected to coordinator " + coordinator.getIp() + " " + coordinator.getPort());
		while(true) {
			try {
				transport.open();
				TProtocol protocol = new TBinaryProtocol(transport);
				KeyValueStore.Client client = new KeyValueStore.Client(protocol);
			
				System.out.println("Enter the operation in the format get/put,key,value,ONE/TWO");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String operation = br.readLine();
				String[] command = operation.split(",");
				int key = Integer.parseInt(command[1]);
				String value = command[2];
				Request request = new Request();
				request.setLevel(ConsistencyLevel.valueOf(command[3]));
				request.setIsCoordinator(true);
				if(command[0].equalsIgnoreCase("get")) {
					String returnedValue = client.get(key, request, new ReplicaID().setId("client"));
					if(returnedValue.isEmpty()) {
						System.out.println("The key either does not exist or has null value");
					} else {
						System.out.println("The value returned for key " + key + " is " + returnedValue);
					}
				} else {
					client.put(key, value, request, null);
				}
				transport.close();
			
			} catch (TException e) {
				System.err.println("Error: " + e.getMessage());
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
