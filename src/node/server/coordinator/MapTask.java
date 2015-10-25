/**
 * 
 */
package node.server.coordinator;

import java.util.HashMap;
import java.util.Map;

import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author dell
 *
 */
public class MapTask implements EntryListener<String, String>{

	private static HazelcastInstance hz;
	private IMap<String, String> mapTask;
	//private IMap<String, String> mapStatusTask;
	private Map<String, String> mapObjTask = new HashMap<String, String>();
	
	private MapTask mt;
	private DirectoryHandler dirHandler;
	private JSONHandler jHandler;
	
	public MapTask(){
		dirHandler = new DirectoryHandler();
		jHandler = new JSONHandler();
	}

	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method listenerOfTask
	 * listener of mapTask<>
	 */
	public void listenerOfTask(){
		mt = new MapTask();
		mapTask = hz.getMap("maptask");
		mapTask.addEntryListener(mt, true);
	}
	
	/*
	 * Method addStateToMap
	 * add state of task to map<>
	 */
	public void addStateToMap(){
		mapTask = hz.getMap("maptask");
		mapTask.clear();
		mapTask.put("state", "execution");
		
	}
	
	/*
	 * Method addMapTask
	 * add task to map<>
	 */
	public void addMapTask(String stringIn){
		JSONObject objparam = (JSONObject)JSONValue.parse(stringIn);
		String status = (String)objparam.get("status");
		String q = (String)objparam.get("q");
		String geocode = (String)objparam.get("geocode");
		String lang = (String)objparam.get("lang");
		String locale = (String)objparam.get("locale");
		String result_type = (String)objparam.get("result_type");
		String count = (String)objparam.get("count");
		String until = (String)objparam.get("until");
		String since_id = (String)objparam.get("since_id");
		String max_id = (String)objparam.get("max_id");
		String include_entities = (String)objparam.get("include_entities");
		String callback = (String)objparam.get("callback");
		
		// put task to mapTask<>
		mapObjTask.put("status", status);
		mapObjTask.put("q", q);
		mapObjTask.put("geocode", geocode);
		mapObjTask.put("lang", lang);
		mapObjTask.put("locale", locale);
		mapObjTask.put("result_type", result_type);
		mapObjTask.put("count", count);
		mapObjTask.put("until", until);
		mapObjTask.put("since_id", since_id);
		mapObjTask.put("max_id", max_id);
		mapObjTask.put("include_entities", include_entities);
		mapObjTask.put("callback", callback);
		
		String objTask = JSONValue.toJSONString(mapObjTask);
		
		mapTask = hz.getMap("maptask");
		mapTask.clear();
		mapTask.put("state", "execution");
		mapTask.put("task", objTask);
		//saveReplicationTask(mapTask);
	}
	
	/*
	 * Method clearMapTask
	 * clearing mapTask<>
	 */
	public void clearMapTask(){
		mapTask = hz.getMap("maptask");
		//mapTask.clear();
		mapTask.put("state", "null");
		mapTask.put("task", "null");
		saveReplicationTask(mapTask);
	}
	
	/*
	 * Method saveReplicationTask
	 * save replication of task
	 */
	private void saveReplicationTask(Map<String, String> task){
		String namefile = dirHandler.getDirReplication()+"/"+"task";
		String jsonText = JSONValue.toJSONString(task);
		jHandler.saveToJSON(jsonText, namefile);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryAdded(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryAdded(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		// save replication if maptask added
		// System.out.println("map task ~ entry added : " + event.getValue());
		mapTask = hz.getMap("maptask");
		saveReplicationTask(mapTask);
	}

	@Override
	public void entryEvicted(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryRemoved(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryUpdated(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO ~ TASK : task updated");
		System.out.println("GRID INFO ~ TASK : "+ event.getKey() +" -> "+ event.getValue());
		System.out.println("");
		System.out.println("==============================================================================");
		mapTask = hz.getMap("maptask");
		saveReplicationTask(mapTask);
	}
	
}
