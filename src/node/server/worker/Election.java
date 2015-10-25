/**
 * Election.java
 * package node.server.worker
 */
package node.server.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
//import com.hazelcast.core.Member;

import node.server.main.DirectoryHandler;
import node.server.worker.MapCoordinator;

/**
 * @author Willi
 * email : willi.ilmukomputer@gmail.com
 */
public class Election {
	
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private static HazelcastInstance hz;
	private String thisMachine = null;
	private Map<Integer, String> firstElection;
	private String coordinatorMap = "coordinator";
	private Map<Integer, String> mapMembers = new HashMap<Integer, String>();
	//private Map<Integer, InetAddress> mapMembersTemp = new HashMap<Integer, InetAddress>();
	private Map<Integer, String> mapMembersSorting = new HashMap<Integer, String>();
	private IMap<String, String> mapCoordinator;
	private Map<String, String> mapCoordinatorTemp = new HashMap<String, String>();
	private Map<Integer, String> mapSorting; 
	private Map<Integer, String> mapElectionTemp;
	private int counterMessage = 1;
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method getHzInstance
	 * get hazelcast instance 
	 */
	public HazelcastInstance getHzInstance(){
		return hz;
	}
	
	/*
	 * Method getCounterMessage
	 * get counter message transmit
	 */
	public int getCounterMessage(){
		return counterMessage;
	}
	
	/*
	 * Method setCounterMessage
	 * set / increment counter message transmit
	 */
	public void setCounterMessage(){
		counterMessage += 1;
	}
	
	/*
	 * Method resetCounterMessage
	 * reset counter message to 0
	 */
	public void resetCounterMessage(){
		counterMessage = 1;
	}
	
	/*
	 * Method getSizeCluster
	 * get size of cluster
	 */
	public int getSizeCluster(){
		return getHzInstance().getCluster().getMembers().size();
	}
	
	/*
	 * Method doElection
	 * do election coordinator
	 */
	public void doElection(String electionMessage) throws ParseException{
		spreadingCoordinator(selectMember(electionMessage));
	}
	
	/*
	 * Method selectMember
	 * select coordinator from members of grid
	 */
	private String selectMember(String electionMessage) throws ParseException{
		JSONParser parser = new JSONParser();
		Object obj;
		obj = parser.parse(electionMessage);
		JSONObject jObj = (JSONObject)obj;
		int sizeMessage = jObj.size();
		String selectedMember = null;
		for(int i=1; i<=sizeMessage; i++){
			String select = jObj.get(String.valueOf(i)).toString();
			String[] address = select.split("-");
			String numMachine = address[0];
			if(numMachine.equals(String.valueOf(sizeMessage))){
				String addressMachine = address[1];
				selectedMember = addressMachine;
			}
		}
		//String[] address = select.split("-");
		//String numMachine = address[0];
		//String addressMachine = address[1];
		//Map<Integer, String> mms = mapMembersSorting();
		//int sizeGrid = mms.size();
		//String selectedMember = mms.get(sizeGrid);
		
		return selectedMember;
	}
	
	/*
	 * method spreadingCoordinator
	 * spreading coordinator & insert into coordinator IMap<>
	 */
	public void spreadingCoordinator(String coordinatorAddress){
		MapCoordinator mc = new MapCoordinator();
		mapCoordinator = hz.getMap(coordinatorMap); 
		if(checkCoordinator(coordinatorAddress) == true){
			String addressCoordinator = mapCoordinator.get(coordinatorMap);
			mapCoordinatorTemp.put(coordinatorMap, addressCoordinator);
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("GRID INFO: GRID COORDINATOR -> " + addressCoordinator);
			System.out.println("GRID INFO: GRID COORDINATOR -> [process] grid coordinator save to file..");
			mc.saveCoordinator(mapCoordinatorTemp);
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("GRID INFO: GRID COORDINATOR -> [success] grid coordinator save to file..");
			System.out.println("");
			System.out.println("==============================================================================");
		}else{
			String cm = mapCoordinator.get(coordinatorMap);
			if(cm == coordinatorAddress){
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO: GRID COORDINATOR -> " + cm);
				System.out.println("GRID INFO: GRID COORDINATOR -> [process] grid coordinator save to file..");
				mc.saveCoordinator(mapCoordinatorTemp);
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO: GRID COORDINATOR -> [success] grid coordinator save to file..");
				System.out.println("");
				System.out.println("==============================================================================");
			}else{
				//mapCoordinator.clear();
				mapCoordinator.put(coordinatorMap, coordinatorAddress);
				String addressCoordinator = mapCoordinator.get(coordinatorMap);
				mapCoordinatorTemp.put(coordinatorMap, addressCoordinator);
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO: GRID COORDINATOR -> " + addressCoordinator);
				System.out.println("GRID INFO: GRID COORDINATOR -> [process] grid coordinator save to file..");
				mc.saveCoordinator(mapCoordinatorTemp);
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO: GRID COORDINATOR -> [success] grid coordinator save to file..");
				System.out.println("");
				System.out.println("==============================================================================");
				//mapCoordinator.addEntryListener(mc, true);
			}
			
		}
		//mc.listenerOfCoordinator();
	}
	
	/*
	 * Method checkCoordinator
	 * check IMap<> of coordinator, already exist or not exist yet.
	 */
	private boolean checkCoordinator(String coordinatorAddress){
		mapCoordinator = hz.getMap(coordinatorMap);
		String alreadyExist = mapCoordinator.get(coordinatorMap);
		if(alreadyExist.equals(coordinatorAddress)){
			return true;
		}
		return false;
	}
	
	/*
	 * Method electingCoordinator
	 * electing coordinator from election message
	 */
	public String electingCoordinator(String electionMessage) throws ParseException{
		JSONParser parser = new JSONParser();
		Object obj;
		obj = parser.parse(electionMessage);
		JSONObject jObj = (JSONObject)obj;
		int sizeMessage = jObj.size();
		for(int i=1; i<=sizeMessage; i++){
			mapElectionTemp.put(i, (String) jObj.get(i));
		}
		String coordinator = mapElectionTemp.get(sizeMessage);
		return coordinator;	
	}
	
	/*
	 * Method firstElectionMessage
	 * create 1st election message
	 */
	public String firstElectionMessage(){
		//thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		thisMachine = getThisMachine();
		firstElection = new HashMap<Integer, String>();

		File file = new File(dirHandler.getDirMember()+"./member.json");
		Boolean fileExist = file.exists();
		if(fileExist == true){
			try {
				JSONObject obj = (JSONObject)JSONValue.parse(new FileReader(dirHandler.getDirMember()+"./member.json"));
				JSONArray arrMember = (JSONArray)obj.get("member");
				int sam = arrMember.size();
				for(int i=0; i<sam; i++){
					JSONObject objarr = (JSONObject)JSONValue.parse(arrMember.get(i).toString());
					@SuppressWarnings("unchecked")
					Set<String> keys = objarr.keySet();
					Iterator<String> it = keys.iterator(); 
					while(it.hasNext()){
						String key = (String)it.next();
						String value = (String)objarr.get(key);
						if(thisMachine.equals(value)){
							firstElection.put(1, key+"-"+value);	
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		//firstElection.put(1, thisMachine);
		//mapElection = new HashMap<Integer, InetAddress>();
		//mapElection.put(1, thisMachine);
		String jsonMapElection = JSONObject.toJSONString(firstElection);
		//mapElection.clear();
		return jsonMapElection;
	}

	/*
	 * Method nextAddress
	 * get next address from members of Grid 
	 */
	public String nextAddress(){
		//Integer counter = 0;
		Integer keyThisMachine = null;
		Integer nextKey = 1;
		Integer mapSize = null;
		// get address of this machine
		thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		mapMembersSorting.clear();
		Map<Integer, String> mms = mapMembersSorting();
		for(Map.Entry<Integer, String> entry : mms.entrySet()){
			if(thisMachine.equals(entry.getValue())){
				keyThisMachine = entry.getKey();
				nextKey = keyThisMachine + 1;
			}
		}
		mapSize = mapMembers.size();
		if((mapSize == 1) | (nextKey > mapSize)){
			nextKey=1;
		}
		String nextAddress = mapMembers.get(nextKey);
		return nextAddress;
	}
	
	/*
	 * Method firstAddress
	 * get last address from members of Grid
	 */
	public String firstAddress(String electionMessage){
		String first = null;
		mapElectionTemp = new HashMap<Integer, String>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(electionMessage);
			JSONObject jObj = (JSONObject)obj;
			
			//String inetFirst = jObj.get(1).toString();
			int sizeMessage = jObj.size();
			for(Integer i=1; i<=sizeMessage; i++){
				String num = i.toString();
				String addr = (String)jObj.get(num);
				mapElectionTemp.put(i,addr);
			}
			mapSorting = new TreeMap<Integer, String>(mapElectionTemp);
			first = mapSorting.get(1);
			mapElectionTemp.clear();
			mapSorting.clear();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return first;
	}
	
	/* 
	 * Method mapMembersSorting
	 * sorting member of grid
	 */
	private Map<Integer, String> mapMembersSorting(){
		Iterator<Member> it = hz.getCluster().getMembers().iterator();
		mapMembers.clear();
		int count = 1;
		while(it.hasNext()){
			mapMembers.put(count, it.next().getInetSocketAddress().getAddress().toString().substring(1));
			count++;
		}
		Integer no = 1;
		//mapMembers = hz.getMap(nMap);
		mapSorting = new TreeMap<Integer, String>(mapMembers);
		for(Map.Entry<Integer, String> entry : mapSorting.entrySet()){
			mapMembersSorting.put(no, entry.getValue());
			no++;
		}
		return mapMembersSorting;
	}
	
	/*
	 * Method getThisMachine
	 * get this machine address
	 */
	public String getThisMachine(){
		thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		return thisMachine;
	}

}
