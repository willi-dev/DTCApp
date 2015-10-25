/**
 * 
 */
package node.server.coordinator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import node.server.main.Oauth;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * @author dell
 *
 */
public class Distributor {

	private static HazelcastInstance hz;
	private String thisMachine;
	
	private Socket destination = null;
	private int paramsport = 4001;
	private OutputStreamWriter outStreamWrite = null;
	
	private Map<Integer, String> mapMembers = new HashMap<Integer, String>();
	private Map<Integer, String> mapMembersSorting = new HashMap<Integer, String>();
	private Map<Integer, String> mapSorting; 
	private static Queue<String> queueAddress = new LinkedList<String>();
	public String resultCoordinator = null;
	public String maxID = "";
	
	private Oauth oauth = new Oauth();
	
	public Distributor(){
		// Constructor
		oauth.setConsumerKey("nYIIxPgqsT10rV2112zM2Q");
		oauth.setConsumerSecret("IoWDuwpYmbwIS4qvUCHRZbl0XikzPvHtBHVg8vYAC4");
	}
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method getThisMachine
	 * get this machine address
	 */
	public String getThisMachine(){
		thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		return thisMachine;
	}
	
	/* 
	 * Method mapMembersSorting
	 * sorting member of grid
	 */
	public Map<Integer, String> mapMembersSorting(){
		//mapMembers.clear();
		Iterator<Member> it = hz.getCluster().getMembers().iterator();
		int count = 1;
		while(it.hasNext()){
			mapMembers.put(count, it.next().getInetSocketAddress().getAddress().toString().substring(1));
			count++;
		}
		Integer no = 1;
		//mapMembers = hz.getMap(nMap);
		//if(!mapSorting.isEmpty()){
		//	mapSorting.clear();
		//}
		//if(!mapMembersSorting.isEmpty()){
		//	mapMembersSorting.clear();
		//}
		mapSorting = new TreeMap<Integer, String>(mapMembers);
		for(Map.Entry<Integer, String> entry : mapSorting.entrySet()){
			mapMembersSorting.put(no, entry.getValue());
			no++;
		}
		return mapMembersSorting;
	}
	
	/*
	 * Method sizeMembersSorting
	 * return size of mapMembersSorting
	 */
	public Integer sizeMembersSorting(){
		return mapMembersSorting().size();
	}
	
	/*
	 * Method queueMembersAddress
	 * return queue of sorted members address
	 */
	public Queue<String> queueMembersAddress(){
		//queueAddress.clear();
		Map<Integer, String> mms = mapMembersSorting();
		for(Map.Entry<Integer, String> entry : mms.entrySet()){
			if(!getThisMachine().equals(entry.getValue())){
				queueAddress.add(entry.getValue());
			}
		}
		//queueAddress.poll();
		return queueAddress;
	}
	
	/*
	 * Method clearQueueMembers
	 * clearing queue members
	 */
	public void clearQueueMembers(){
		queueAddress.clear();
	}
	
	/*
	 * Method sendToQueue
	 * send params to member's address from queue
	 */
	public void sendToQueue(String pollAddress, String paramsIn) throws UnknownHostException, IOException{
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : DISTRIBUTOR ~ Kirim task & parameters ke : " + pollAddress);
		System.out.println("");
		System.out.println("==============================================================================");
		//destination = new Socket(pollAddress, paramsport);
		destination = new Socket();
		destination.connect(new InetSocketAddress(pollAddress, paramsport),10000);
		outStreamWrite = new OutputStreamWriter(destination.getOutputStream(), "UTF-8");
		outStreamWrite.write(paramsIn,0, paramsIn.length());
		outStreamWrite.close();
		destination.close();
	}
	
	/*
	 * Method setResultCoordinator
	 * set result from coordinator value
	 */
	public void setResultCoordinator(String resultcoordinator){
		resultCoordinator = resultcoordinator;
	}
	
	/*
	 * Method getResultCoordinator
	 * return search result from coordinator
	 */
	public String getResultCoordinator(){
		return resultCoordinator;
	}
	
	/*
	 * Method getSizeQueue
	 * return size of queueAddress
	 */
	public Integer getSizeQueue(){
		return queueAddress.size();
	}
	
	/*
	 * Method emptyMaxID
	 * emptying max id from last search
	 */
	public void emptyMaxID(){
		maxID = "";
	}
	
	/*
	 * Method setMaxID
	 * set max id from last search
	 */
	public void setMaxID(String maxId){
		maxID = maxId;
	}
	
}
