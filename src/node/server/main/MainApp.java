package node.server.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import node.server.coordinator.MapMember;
import node.server.coordinator.MapTask;
import node.server.coordinator.ReplicationResult;
import node.server.main.ClientHandler;
import node.server.worker.MapCoordinator;
//import node.server.coordinator.MapWorker;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.IMap;
//import com.hazelcast.core.IMap;

public class MainApp {
	/**
	 * @param args
	 */
	static Socket client = null;
	static ServerSocket server = null;
	
	private static Client cl = null;
	private static ClientHandler clHandler = null;
	private static HazelcastInstance hi;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			socketServerOpen(4001); // open socket server
			Config cfg = new Config();
			cfg.setProperty("hazelcast.logging.type", "none"); // set hazelcast logging to none
			hi = Hazelcast.newHazelcastInstance(cfg);
			
			MapMember member = new MapMember();
			MapCoordinator coordinator = new MapCoordinator();
			MapTask task = new MapTask();
			ReplicationResult replicaresult = new ReplicationResult();
			
			// set hazelcast instance to member and coordinator
			member.setHzInstance(hi);
			coordinator.setHzInstance(hi);
			
			// call listener membership
			member.listenerOfMember();
			
			/*
			 * checkMapCoordinator() -> checking map coordinator
			 * showCoordiantor() -> showing coordinator
			 * listenerOfCoordinator() -> call listener of coordinator event
			 * gridMembers() -> showing members of grid
			 */
			coordinator.checkMapCoordinator(); 
			coordinator.showCoordinator(); 
			coordinator.listenerOfCoordinator();
			member.gridMembers();
			
			cl = new Client();
			cl.setHzInstance(hi);
			
			/*
			 * setHzInstance() -> set hazelcast instance to task 
			 * listenerOfTask() -> call listener of task event
			 */
			task.setHzInstance(hi);
			task.listenerOfTask();
			
			/*
			 * setHzInstance() -> set hazelcast instance to replica
			 * listenerOfMultiMapResult() -> call listener of result(multimap)
			 */
			replicaresult.setHzInstance(hi);
			replicaresult.listenerOfMultimapResult();
			
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("GRID INFO: CRAWLER [MENUNGGU TASK]");
			System.out.println("");
			System.out.println("==============================================================================");
			
			while(true){
				client = server.accept();
				clHandler = new ClientHandler(client);
				clHandler.clientExecute();
			}
			
			//socketServerClose();
			
		}catch(IOException ioEx){
			ioEx.printStackTrace();
		}
		//Hazelcast.shutdownAll();	
	}

	/*
	 * Method socketServerOpen
	 * open port socket for accepting connection from client
	 */
	public static void socketServerOpen(Integer port) throws IOException{
		server = new ServerSocket(port);
	}
	public static void socketServerClose() throws IOException{
		server.close();
	}
}
