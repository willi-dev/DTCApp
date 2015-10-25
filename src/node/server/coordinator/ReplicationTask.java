/**
 * 
 */
package node.server.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.hazelcast.core.HazelcastInstance;

/**
 * @author dell
 *
 */
public class ReplicationTask {

	private static JSONHandler jHandler = new JSONHandler();
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	
	private static HazelcastInstance hz;
	private Map<Integer, String> mapMembers = new HashMap<Integer, String>();
	private Map<Integer, String> mapMembersSorting = new HashMap<Integer, String>();
	private Map<Integer, String> mapSorting;
	private static Queue<String> queueAddress = new LinkedList<String>();
	
	private Map<String, String> mapObjTask = new HashMap<String, String>();
	private Map<String, String> mapTask = new HashMap<String, String>();
	private Map<String, String> mapObjTaskInput = new HashMap<String, String>();
	private Map<String, String> mapTaskInput = new HashMap<String, String>();
	private String thisMachine;
	
	private Socket destination = null;
	private int paramsport = 4001;
	private OutputStreamWriter outStreamWrite = null;
	
	/*
	 * constructor
	 */
	public ReplicationTask(){
		
	}
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
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
		
		mapTask.clear();
		mapTask.put("status", "replicationtask");
		mapTask.put("task", objTask);
		//saveReplicationTask(mapTask);
	}
	
	/*
	 * Method getMapTask
	 */
	public Map<String, String> getMapTask(){
		return mapTask;
	}
	
	public void taskInput(String stringIn){
		JSONObject objparam = (JSONObject)JSONValue.parse(stringIn);
		String status = (String)objparam.get("status");
		String task = (String)objparam.get("task");
		JSONObject objtask = (JSONObject)JSONValue.parse(task);
		String statusinput = (String)objtask.get("status");
		String q = (String)objtask.get("q");
		String geocode = (String)objtask.get("geocode");
		String lang = (String)objtask.get("lang");
		String locale = (String)objtask.get("locale");
		String result_type = (String)objtask.get("result_type");
		String count = (String)objtask.get("count");
		String until = (String)objtask.get("until");
		String since_id = (String)objtask.get("since_id");
		String max_id = (String)objtask.get("max_id");
		String include_entities = (String)objtask.get("include_entities");
		String callback = (String)objtask.get("callback");
		
		// put task to mapTask<>
		mapObjTaskInput.put("status", statusinput);
		mapObjTaskInput.put("q", q);
		mapObjTaskInput.put("geocode", geocode);
		mapObjTaskInput.put("lang", lang);
		mapObjTaskInput.put("locale", locale);
		mapObjTaskInput.put("result_type", result_type);
		mapObjTaskInput.put("count", count);
		mapObjTaskInput.put("until", until);
		mapObjTaskInput.put("since_id", since_id);
		mapObjTaskInput.put("max_id", max_id);
		mapObjTaskInput.put("include_entities", include_entities);
		mapObjTaskInput.put("callback", callback);
		
		String objTask = JSONValue.toJSONString(mapObjTaskInput);
		
		mapTaskInput.clear();
		mapTaskInput.put("status", status);
		mapTaskInput.put("task", objTask);
	}
	
	/*
	 * Method getMapTaskInput
	 */
	public Map<String, String> getMapTaskInput(){
		return mapTaskInput;
	}
	
	/*
	 * Method clearMapTask
	 * clearing mapTask<>
	 */
	public void clearMapTask(){
		//mapTask.clear();
		mapTask.put("status", "null");
		mapTask.put("task", "null");
		saveReplicationTask(mapTask);
	}
	
	/*
	 * Method saveReplicationTask
	 * save replication of task
	 */
	public void saveReplicationTask(Map<String, String> task){
		String namefile = dirHandler.getDirReplication()+"/"+"task";
		String jsonText = JSONValue.toJSONString(task);
		jHandler.saveToJSON(jsonText, namefile);
	}
	
	/*
	 * Method getSizeMembers
	 * return size of members
	 */
	public int getSizeMembers(){
		return mapMembers.size()-1;
	}
	
	/*
	 * Method setMembers
	 * show member from mapMembers
	 */
	public void setMembers(){	
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
					int count = 1;
					while(it.hasNext()){
						String key = (String)it.next();
						String value = (String)objarr.get(key);
						//System.out.println("	" + key + " - " + value);	
						mapMembers.put(i+1, value);
						count++;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			// nothing
			System.out.println("ERROR, NO MEMBER");
		}
		
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
	 * Method mapMembersSorting
	 * sorting member of grid
	 */
	public Map<Integer, String> mapMembersSorting(){
		Integer no = 1;
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
	
	/*
	 * Method sendToQueue
	 * send replication result to member's address from queue
	 */
	public void sendToQueue(String pollAddress, String replicationTask) throws UnknownHostException, IOException{
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : REPLIKASI ~ Kirim replikasi hasil ke : " + pollAddress);
		System.out.println("");
		System.out.println("==============================================================================");
		destination = new Socket(pollAddress, paramsport);
		outStreamWrite = new OutputStreamWriter(destination.getOutputStream(), "UTF-8");
		outStreamWrite.write(replicationTask,0, replicationTask.length());
		outStreamWrite.close();
		destination.close();
	}
	
}
