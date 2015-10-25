package node.server.main;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler{
	
//	private static Map<String, String> params;
//	private static Map<String, String> execParams;
//	private static TaskQueue taskqueue = new TaskQueue();
	String clientParamsIn = null;
	
	protected static Socket client;
	private ClientReceiver clReceive;
	private Client cl;
	
	public ClientHandler(Socket socket) throws IOException{
		client = socket; 
		cl = new Client();
		cl.setClient(client);
		clientParamsIn = cl.receiveStringFromClient();
	}
	
	/* 
	 * Method clientExecute
	 * execute request from client and run client receiver thread
	 */
	public void clientExecute() throws IOException{
		clReceive = new ClientReceiver(clientParamsIn);
		clReceive.start();
	}
	
	
	
}
