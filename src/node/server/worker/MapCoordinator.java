package node.server.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import node.server.main.ClientReceiver;
import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class MapCoordinator implements EntryListener<String, String>{

	private static JSONHandler jHandler = new JSONHandler();
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private MapCoordinator mc;
	private IMap<String, String> mapCoordinator;
	private Map<String, String> mapTempCoordinator = new HashMap<String, String>();
	private String nMap = "coordinator";
	private String status = "coordinator";
	private HazelcastInstance hz;
	private ClientReceiver clReceive;
	private String thisMachine = null;
	private Election el;
	
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method listenerOfCoordinator
	 * listener of coordinator entry
	 */
	public void listenerOfCoordinator(){
		mc = new MapCoordinator();
		mapCoordinator = hz.getMap(nMap);
		mapCoordinator.addEntryListener(mc, true);
	}
	
	public void showCoordinator(){
		mapCoordinator = hz.getMap(nMap);
		System.out.println("GRID INFO: GRID COORDINATOR :");
		System.out.println("     		COORDINATOR -> " + mapCoordinator.get("coordinator"));
	}
	
	public void checkMapCoordinator(){
		//mc = new MapCoordinator();
		mapCoordinator = hz.getMap(nMap);
		if(mapCoordinator.isEmpty()){
			System.out.println("GRID INFO: GRID COORDINATOR -> There is no coordinator");
			System.out.println("GRID INFO: GRID COORDINATOR -> select coordinator from 1st member");
			insertMapCoordinator();
		}else{
			String addressCoordinator = mapCoordinator.get(nMap);
			mapTempCoordinator.put(status, addressCoordinator);
			System.out.println("GRID INFO: GRID COORDINATOR -> " + addressCoordinator);
			System.out.println("GRID INFO: GRID COORDINATOR -> [process] grid coordinator save to file..");
			saveCoordinator(mapTempCoordinator);
			System.out.println("GRID INFO: GRID COORDINATOR -> [success] grid coordinator save to file..");
			//mapCoordinator.addEntryListener(mc, true);
		}
	}
	
	/*
	 * Method insertMapCoordinator
	 * insert coordinator to map<>
	 */
	private void insertMapCoordinator() {
		// TODO Auto-generated method stub 
		//mc = new MapCoordinator();
		Iterator<Member> it = hz.getCluster().getMembers().iterator();
		mapCoordinator = hz.getMap(nMap); 
		int count = 1;
		while(it.hasNext()){
			mapCoordinator.put(status, it.next().getInetSocketAddress().getAddress().toString().substring(1));
			//mapTempCoordinator.put(status , it.next().getInetSocketAddress());
			count++;
		}
		String addressCoordinator = mapCoordinator.get(nMap);
		mapTempCoordinator.put(status, addressCoordinator);
		System.out.println("GRID INFO: GRID COORDINATOR -> " + addressCoordinator);
		System.out.println("GRID INFO: GRID COORDINATOR -> [process] grid coordinator save to file..");
		saveCoordinator(mapTempCoordinator);
		System.out.println("GRID INFO: GRID COORDINATOR -> [success] grid coordinator save to file..");
		//mapCoordinator.addEntryListener(mc, true);
	}
	
	/*
	 * Method saveCoordinator
	 * save elected coordinator
	 */
	public void saveCoordinator(Map<String, String> coordinator){
		String namefile = dirHandler.getDirCoordinator()+"/"+"coordinator";
		String jsonText = JSONValue.toJSONString(coordinator);
		jHandler.saveToJSON(jsonText, namefile);
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
	 * @see com.hazelcast.core.EntryListener#entryAdded(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryAdded(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("GRID INFO: GRID COORDINATOR -> MAP COORDINATOR ADDED : " );
		System.out.println("		" + event.getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryEvicted(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryEvicted(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("Map Coordinator evicted : " );
		System.out.println("		" + event.getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryRemoved(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryRemoved(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("Map Coordinator removed : " );
		System.out.println("		" + event.getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryUpdated(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryUpdated(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("GRID INFO: GRID COORDINATOR -> MAP COORDINATOR UPDATED " );
		System.out.println("GRID INFO: NEW COORDINATOR -> "  + event.getValue());
		
		el = new Election();
		
		thisMachine = el.getThisMachine();
		//System.out.println(thisMachine);
		if(thisMachine.equals(event.getValue())){
			try {
				if(getTask()!=null){
					//System.out.println("task available");
					clReceive = new ClientReceiver(getTask());
					clReceive.start();
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
