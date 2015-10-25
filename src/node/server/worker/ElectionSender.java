/**
 * 
 */
package node.server.worker;

import java.io.IOException;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import node.server.worker.Election;

/**
 * @author Willi
 * email : willi.ilmukomputer@gmail.com
 */
public class ElectionSender extends Thread{

	private Election el;
	private Socket nextWorker = null;
	private static OutputStreamWriter outStreamWrite = null;
	private static Integer electionPort = 4444;
	
	public ElectionSender(){
		el = new Election();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		sendElectionMessage();
	}
	
	/*
	 * Method sendElectionMessage
	 * sending election message to another members of Grid.
	 */
	private void sendElectionMessage(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
		try{
			nextWorker = new Socket(el.nextAddress(), electionPort);
			System.out.println("GRID INFO: ELECTION [SEND MESSAGE]");
			System.out.println(dt.format(cal.getTime()) +" -> [kirim] election message to -> next member: " + el.nextAddress());
			System.out.println("GRID INFO: ELECTION [SEND MESSAGE]");
			System.out.println(dt.format(cal.getTime()) +" -> [kirim] content of election messsage -> : " + el.firstElectionMessage());
			outStreamWrite = new OutputStreamWriter(nextWorker.getOutputStream(), "UTF-8");
			//outStream = nextWorker.getOutputStream();
			outStreamWrite.write(el.firstElectionMessage(),0, el.firstElectionMessage().length());
			System.out.println("==============================================================================");
			System.out.println("");
			outStreamWrite.close();
			nextWorker.close();
		}catch(UnknownHostException e){
			System.err.println(e.getMessage());
		}catch(IOException e){
			System.err.println(e.getMessage());
		}
	}
}
