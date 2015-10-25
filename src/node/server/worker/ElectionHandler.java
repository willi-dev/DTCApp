/**
 * 
 */
package node.server.worker;

/**
 * @author Willi
 * email : willi.ilmukomputer@gmail.com
 */
public class ElectionHandler{

	private ElectionReceiver electionReceive;
	private ElectionSender electionSend;
	
	/*
	 * Method ElectionReceiverThread
	 * running election receiver thread
	 */
	public void electionReceiverThread(){
		electionReceive = new ElectionReceiver();
		electionReceive.start();
	}
	
	/*
	 * Method electionSenderThread
	 * running election sender thread
	 */
	public void electionSenderThread(){
		electionSend = new ElectionSender();
		electionSend.start();
	}
	
	/*
	 * Method getThreadState
	 * get state of thread
	 */
	public String ThreadState(Thread thread){
		return thread.getState().toString();
	}
	
	/*
	 * Method getStateReceiver
	 * get state of receiver
	 */
	public String getStateReceiver(){
		electionReceive = new ElectionReceiver();
		return ThreadState(electionReceive);
	}
	
	/*
	 * Method getStateSender
	 * get state of sender
	 */
	public String getStateSender(){
		electionSend = new ElectionSender();
		return ThreadState(electionSend);
	}
}
