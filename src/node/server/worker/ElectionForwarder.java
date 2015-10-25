/**
 * 
 */
package node.server.worker;

import java.io.IOException;
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
public class ElectionForwarder extends Thread{

	private Election el;
	private Socket nextWorker = null;
	private static OutputStreamWriter outStreamWrite = null;
	private static Integer electionPort = 4444;
	private static String electionMessageForward;
	
	public ElectionForwarder(String elMsgForward){
		el = new Election();
		electionMessageForward = elMsgForward;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		try {
			forwardElectionMessage();
			//forwardElectionMessageUDP();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Method forwardElectionMessage
	 * forwarding election message to next member of Grid.
	 */
	private void forwardElectionMessage() throws IOException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
		try{ 
			nextWorker = new Socket(el.nextAddress(), electionPort);
			System.out.println("GRID INFO: ELECTION [FORWARD MESSAGE]");
			System.out.println(dt.format(cal.getTime()) +" -> [forward] -> next member : " + el.nextAddress());
			System.out.println("GRID INFO: ELECTION [FORWARD MESSAGE]");
			System.out.println(dt.format(cal.getTime()) +" -> [forward] election message : " + electionMessageForward);
			System.out.println("============================================================================");
			System.out.println("");
			outStreamWrite = new OutputStreamWriter(nextWorker.getOutputStream(), "UTF-8");
			outStreamWrite.write(electionMessageForward,0, electionMessageForward.length());
			
			outStreamWrite.close();
			nextWorker.close();
		}catch(UnknownHostException e){
			System.err.println(e.getMessage());
		}catch(IOException e){
			System.err.println(e.getMessage());
		}
	}
}
