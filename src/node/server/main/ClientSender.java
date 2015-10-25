/**
 * 
 */
package node.server.main;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author dell
 *
 */
public class ClientSender extends Thread {

	private String paramsIn = null;
	private Socket destination = null;
	private int paramsport = 4001;
	private OutputStreamWriter outStreamWrite = null;
	
	private Client cl;
	
	public ClientSender(String params){
		paramsIn = params;
		cl = new Client();
	}
	
	public void run(){
		sendParamsToCoordinator();
	}
	
	private void sendParamsToCoordinator(){
		try {
			destination = new Socket(cl.getCoordinator(), paramsport);
			outStreamWrite = new OutputStreamWriter(destination.getOutputStream(), "UTF-8");
			outStreamWrite.write(paramsIn,0, paramsIn.length());
			outStreamWrite.close();
			destination.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
