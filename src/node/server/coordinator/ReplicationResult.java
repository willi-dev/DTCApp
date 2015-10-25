/**
 * 
 */
package node.server.coordinator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;

/**
 * @author Willi
 * willi.ilmukomputer@gmail.com
 */
public class ReplicationResult implements EntryListener<String, String> {
	
	private static HazelcastInstance hz;
	
	private String thisMachine;
	private Map<Integer, String> mapMembers = new HashMap<Integer, String>();
	private Map<Integer, String> mapMembersSorting = new HashMap<Integer, String>();
	private Map<Integer, String> mapSorting; 
	private MultiMap<String, String> multiMapResult;
	private IMap<String, String> mapNameFile;
	private String multimapresult = "multimapresult";
	private static Queue<String> queueAddress = new LinkedList<String>();
	public String namefile;
	
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private static JSONHandler jHandler = new JSONHandler();
	//private static String pathFileResult = null; 
	private ReplicationResult replicaresult;
	
	private Socket destination = null;
	private int paramsport = 4001;
	private OutputStreamWriter outStreamWrite = null;
	
	public ReplicationResult(){
		// constructor
	}
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	public void listenerOfMultimapResult(){
		replicaresult = new ReplicationResult();
		multiMapResult = hz.getMultiMap(multimapresult);
		multiMapResult.addEntryListener(replicaresult, true);
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
		mapSorting = new TreeMap<Integer, String>(mapMembers);
		for(Map.Entry<Integer, String> entry : mapSorting.entrySet()){
			mapMembersSorting.put(no, entry.getValue());
			no++;
		}
		return mapMembersSorting;
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
	 * Method sendToQueue
	 * send replication result to member's address from queue
	 */
	public void sendToQueue(String pollAddress, String replicationResult) throws UnknownHostException, IOException{
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : REPLIKASI ~ Kirim replikasi hasil ke : " + pollAddress);
		System.out.println("");
		System.out.println("==============================================================================");
		destination = new Socket(pollAddress, paramsport);
		outStreamWrite = new OutputStreamWriter(destination.getOutputStream(), "UTF-8");
		outStreamWrite.write(replicationResult,0, replicationResult.length());
		outStreamWrite.close();
		destination.close();
	}
	
	/*
	 * Method saveReplicaResult
	 * save result of search
	 */
	public void saveReplicaResult(String namefile, String statuses){
		// save result
		saveResult(namefile, statuses);
		System.out.println("");
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : REPLIKASI HASIL SEARCH TELAH DISIMPAN!");
		System.out.println("");
		System.out.println("==============================================================================");
	}
	
	/*
	 * Method saveResult
	 * save result of Search
	 */
	private void saveResult(String namefile, String getSearch){
		//String directoryresult = "results"; 
		//pathFileResult = "dtcapp/results/result_"+ namefile;
		String pathNamefile = dirHandler.getDirResult()+"/"+"result_"+ namefile;
		//saveToJSON(getSearch, directoryresult, namefile);
		jHandler.saveToJSON(getSearch, pathNamefile);
	}
	
	/*
	 * Method setNameFile
	 * set name file
	 */
	public void setNameFile(String nf){
		namefile = nf;
		mapNameFile = hz.getMap("mapnamefile");
		mapNameFile.put("namefile", nf);
	}
	
	/*
	 * Method getNameFile
	 * get name file
	 */
	public String getNameFile(){
		mapNameFile = hz.getMap("mapnamefile");
		return mapNameFile.get("namefile");
	}
	
	/*
	 * Method setMultiMapResult
	 * set multimap result
	 */
	public void setMultiMapResult(String nf, String destinationAddress){
		multiMapResult = hz.getMultiMap(multimapresult);
		multiMapResult.put(nf, destinationAddress);
	}
	
	/*
	 * Method saveMappingResult
	 * save mapping result
	 */
	@SuppressWarnings("unchecked")
	private void saveMappingResult(String nf){
		StringBuilder strbuilder = new StringBuilder();
		JSONObject jobj = new JSONObject();
		JSONArray jarr = new JSONArray();
		String objstr;
		
		multiMapResult = hz.getMultiMap("multimapresult");
		
		Set<String> keySet = multiMapResult.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		
		int counter = 1;
		int sizeKeySet = keySet.size();
		while(keyIterator.hasNext()){
			Object key = keyIterator.next();
			//System.out.println("key : " +key);
			Collection<String> cl = multiMapResult.get(key.toString());
			for(String str : cl){
				//System.out.println("value : " +str);
				jarr.add(str);
			}
			jobj.put(key, jarr);
			objstr = jobj.toJSONString();
			int lObj = objstr.length();
			String subObjStr = objstr.substring(1, lObj-1);
			
			
			if(counter==1){
				if(sizeKeySet == 1){
					strbuilder = new StringBuilder(subObjStr);
				}else{
					strbuilder = new StringBuilder(subObjStr);					
					strbuilder = strbuilder.append(",");
				}
			}else{
				strbuilder = strbuilder.append(subObjStr);
				if(counter < sizeKeySet){
					strbuilder = strbuilder.append(",");
				}
			}
			counter++;
			jarr.clear();
			jobj.clear();
		}
		saveMappingJSON("{"+strbuilder.toString()+"}");
		jarr.clear();
		jobj.clear();
		counter = 0;
		
		//saveMappingJSON();
	}
	
	private void saveMappingJSON(String objstring){
		String pathNamefile = dirHandler.getDirReplication()+"/"+"mappingresult";
		//saveToJSON(getSearch, directoryresult, namefile);
		jHandler.saveToJSON(objstring, pathNamefile);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryAdded(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryAdded(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("ENTRY ADDED");
		System.out.println("GRID INFO: MAPPING REPLIKASI TELAH DISIMPAN");
		System.out.println("GRID INFO: ENTRY "+ event.getValue());
		String namafile = getNameFile();
		System.out.println("nama file : " +namafile);
		saveMappingResult(namafile);
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryEvicted(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryEvicted(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryRemoved(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryRemoved(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.hazelcast.core.EntryListener#entryUpdated(com.hazelcast.core.EntryEvent)
	 */
	@Override
	public void entryUpdated(EntryEvent<String, String> event) {
		// TODO Auto-generated method stub
		System.out.println("ENTRY UPDATED");
		System.out.println("GRID INFO: MAPPING REPLIKASI TELAH DISIMPAN");
		String namafile = getNameFile();
		System.out.println("nama file : " +namafile);
		saveMappingResult(namafile);
	}
}
