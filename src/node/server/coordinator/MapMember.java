package node.server.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

import node.server.main.ClientReceiver;
import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;
import node.server.worker.Election;
import node.server.worker.ElectionHandler;
import node.server.coordinator.Distributor;

public class MapMember implements MembershipListener{

	private static JSONHandler jHandler = new JSONHandler();
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private MapMember mm;
	private static HazelcastInstance hz; 
	private IMap<String, String> mapCoordinator; 
	//private IMap<Integer, InetAddress> mapMembers;
	private Map<Integer, String> mapMembers = new HashMap<Integer, String>();
	//private Map<Integer, String> mapMembersTemp = new HashMap<Integer, String>();
	private Map<Integer, String> mapMembersSorting = new HashMap<Integer, String>();
	private Map<Integer, String> mapSorting;
	//private Map<Integer, String> mapMemberFromJSON = new HashMap<Integer, String>();
	private ClientReceiver clReceive;
	
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method listenerOfMember
	 * listen every membership event that changes list members
	 */
	public void listenerOfMember(){
		mm = new MapMember();
		hz.getCluster().addMembershipListener(mm);
	}
	
	/*
	 * Method gridMembers
	 * call insertMembersToMap & showGridMembers
	 */
	public void gridMembers(){
		insertMembersToMap();
		showGridMembers();
	}
	
	/*
	 * Method showGridMembers
	 * show member from mapMembers
	 */
	public void showGridMembers(){	
		/*Integer counter = 1;
		//mapMembers = hz.getMap(nMap);
		mapMembersSorting.clear();
		Map<String, String> mms = mapMembersSorting();
		System.out.println("GRID INFO: GRID MEMBER ~ TOTAL MEMBER(S) -> " + mms.size() + " member(s)");
		System.out.println("GRID INFO: GRID MEMBER ~ MEMBER(S) -> ");
		for(Map.Entry<String, String> entry : mms.entrySet()){
			System.out.println("	" + entry.getKey() + " - " + entry.getValue());	
			counter++;
		}
		*/
		
		System.out.println("GRID INFO: GRID MEMBER ~ MEMBER(S) -> ");
		
		File file = new File(dirHandler.getDirMember()+"./member.json");
		Boolean fileExist = file.exists();
		if(fileExist == true){
			// if member.json available
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
						System.out.println("	" + key + " - " + value);	
					}
				}
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			// nothing
			System.out.println("     ERROR, NO MEMBER");
		}
		
	}
	
	/* 
	 * Method mapMembersSorting
	 * sorting member of grid
	 */
	public Map<Integer, String> mapMembersSorting(){
		/*
		File file = new File(dirHandler.getDirMember()+"./member.json");
		Boolean fileExist = file.exists();
		if(fileExist == true){
			// if task.json & task available
			try {
				JSONObject obj = (JSONObject)JSONValue.parse(new FileReader(dirHandler.getDirMember()+"./member.json"));
				@SuppressWarnings("unchecked")
				Set<String> keys = obj.keySet();
				Iterator<String> it = keys.iterator(); 
				while(it.hasNext()){
					String key = (String)it.next();
					String value = (String)obj.get(key);
					System.out.println("	" + key + " - " + value);	
					//mapMemberFromJSON.put(key, value);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		Integer no = 1;
		//mapMembers = hz.getMap(nMap);
		//mapSorting = new TreeMap<String, String>(mapMembers);
		mapSorting = new TreeMap<Integer, String>(mapMembers);
		for(Map.Entry<Integer, String> entry : mapSorting.entrySet()){
			mapMembersSorting.put(no, entry.getValue());
			no++;
		}
		return mapMembersSorting;
	}
	
	/*
	 * Method insertMembersToMap
	 * get members of grid from hazelcast instance cluster, insert to mapMembers, and save to JSON format
	 */
	@SuppressWarnings("unchecked")
	private void insertMembersToMap() {
		// TODO Auto-generated method stub
		
		Iterator<Member> it = hz.getCluster().getMembers().iterator();
		JSONObject obj = new JSONObject();
		JSONArray listmember = new JSONArray();
		
		//mapMembers = hz.getMap(nMap);
		mapMembers.clear();
		int count = 1;
		while(it.hasNext()){
			mapMembers.put(count, it.next().getInetSocketAddress().getAddress().toString().substring(1));
			count++;
		}
		
		//mapMembersSorting.clear();
		//Map<Integer, String> mms = mapMembersSorting();
		Map<Integer, String> mms = mapMembers;
		Map<Integer, String> mmstemp = new HashMap<Integer, String>();
		//mapMembersTemp.clear();
		//int no = 1;
		for(Map.Entry<Integer, String> entry : mms.entrySet()){
			//mapMembersTemp.put(entry.getKey(), entry.getValue());
			//no++;
			mmstemp.clear();
			mmstemp.put(entry.getKey(), entry.getValue());
			String objArr = JSONValue.toJSONString(mmstemp);
			listmember.add(objArr);
		}
		obj.put("member", listmember);
		String objstr = obj.toJSONString();
		
		//String objstr = JSONValue.toJSONString(mapMembers);
		//System.out.println("GRID INFO: GRID MEMBER SIZE (MAP MEMBERS) -> " + mapMembers.size());
		//System.out.println("GRID INFO: GRID MEMBER SIZE (MAP MEMBERS TEMP) -> " + mapMembersTemp.size());
		System.out.println("GRID INFO: GRID MEMBER -> [process] grid member(s) save to file..");
		
		saveMember(objstr);
		System.out.println("GRID INFO: GRID MEMBER -> [success] grid member(s) save to file..");
	}
	
	/*
	 * Method saveMember
	 * save mapMembers to JSON format
	 */
	private void saveMember(String objectString){
		String namefile = dirHandler.getDirMember()+"/"+"member";
		//String jsonText = JSONValue.toJSONString(member);
		jHandler.saveToJSON(objectString, namefile);
	}
	
	/*
	 * Method checkTask
	 * checking available task
	 */
	public String getTask() throws FileNotFoundException{
		File file = new File(dirHandler.getDirReplication()+"./task.json");
		Boolean fileExist = file.exists();
		if(fileExist == true){
			// if task.json & task available
			JSONObject obj = (JSONObject)JSONValue.parse(new FileReader(dirHandler.getDirReplication()+"./task.json"));
			String task = (String)obj.get("task");
			return task;
		}else{
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.MembershipListener#memberAdded(com.hazelcast.core.MembershipEvent)
	 */
	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		// TODO Auto-generated method stub
		Distributor dist = new Distributor();
		// clearing queue members on distributor.
		dist.clearQueueMembers();
		
		System.out.println("GRID INFO: GRID MEMBER ADDED : " );
		System.out.println("		" + membershipEvent.getMember().getInetSocketAddress().getAddress().toString().substring(1));
		
		insertMembersToMap();
		showGridMembers();
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.MembershipListener#memberRemoved(com.hazelcast.core.MembershipEvent)
	 */
	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		// TODO Auto-generated method stub
		Distributor dist = new Distributor();
		// clearing queue members on distributor.
		dist.clearQueueMembers();
		
		System.out.println("GRID INFO: GRID MEMBER REMOVED : " );
		System.out.println("		" + membershipEvent.getMember().getInetSocketAddress().getAddress().toString().substring(1));

		insertMembersToMap();
		showGridMembers();
		
		/*
		 * stop / suspend clientReceiver thread
		 */
		try {
			if(getTask()!=null){
				//System.out.println("task available");
				clReceive = new ClientReceiver(getTask());
				clReceive.suspendThread();
			}else{
				System.out.println("task not available");
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// Do Election if removed member is coordinator
		String removeAddress = membershipEvent.getMember().getInetSocketAddress().getAddress().toString().substring(1);
		
		mapCoordinator = hz.getMap("coordinator");
		String mcAddress = mapCoordinator.get("coordinator");
		if(mcAddress.equals(removeAddress)){
			System.out.println("");
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("GRID INFO: GRID COORDINATOR IS DOWN...");
			System.out.println("GRID INFO: GRID ~ ELECTING NEW COODINATOR...");
			System.out.println("");
			System.out.println("==============================================================================");
			System.out.println("");
			//mapCoordinator.clear(); // clear map coordinator
			
			Election el = new Election();
			ElectionHandler electionHandler = new ElectionHandler();
			
			el.setHzInstance(hz);
			
			electionHandler.electionReceiverThread();
			if(el.getCounterMessage()== 1){
				electionHandler.electionSenderThread();
			}
			//System.out.println(electionHandler.getStateReceiver());
		}else{
			
			String thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
			if(mcAddress.equals(thisMachine)){
				
				/*
				 * check task.json
				 * and do again crawling tweet
				 */
				/*
				 * resume clientReceiver Thread
				 */
				try {
					if(getTask()!=null){
						//System.out.println("task available");
						clReceive = new ClientReceiver(getTask());
						//clReceive.start();
						clReceive.resumeThread();
					}else{
						System.out.println("task not available");
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	
}
