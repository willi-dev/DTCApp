/**
 * 
 */
package node.server.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;
//import java.util.Set;

import node.server.coordinator.Distributor;
import node.server.coordinator.MapMember;
import node.server.coordinator.MapStateResult;
//import node.server.coordinator.MapTask;
import node.server.coordinator.ReplicationResult;
import node.server.coordinator.ReplicationTask;
import node.server.worker.MapCoordinator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author willi
 * email : willi.ilmukomputer@gmail.com
 */
public class ClientReceiver extends Thread{

	private Socket coordSocket = null;
	private static OutputStreamWriter outStreamWrite = null;
	private static Integer coordPort = 4001;
	protected static Socket client;
	private String stringIn;
	//private static String paramsIn = "";
	private String paramsWithMaxId;
	private String maxId;
	private String pollAddress;
	private static Integer resultIn = 0;
	private Map<String, String> mapParams = new HashMap<String, String>();
	private String status = "fromuser";
	private StringBuilder resultSearch = new StringBuilder(); 
	private String statuses;
	private int gsrLength;
	private String gsr;
	private static String ks = null;
	private String pathResultforCLient = null;
	private static String clientParams = null;
	private String replicaOfResult;
	private String nameFile;
	private String nameFileObject;
	//private String keywordresult;
	
	private static HazelcastInstance hz;
	private IMap<String, String> mapResultToClient;
	private IMap<Integer, String> mapResultTemp;
	
	private Downloader downloader = new Downloader();
	private MapMember member = new MapMember();
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private MapCoordinator coordinator;
	//private MapTask task;
	private MapStateResult mapStateResult;
	
	private Client cl;
	private ClientSender clSend;
	private Distributor dist;
	private ReplicationTask replicatask;
	private ReplicationResult replicaresult;
	
	private String destinationAddress;
	
	boolean suspended = false;
	
	public ClientReceiver(String In) throws IOException{
		/*
		 * set up reference to associated socket...
		 */
		//client = cl.sesocket;
		cl = new Client();
		
		/*
		 * set hazelcast instance from client class
		 */
		setHz(); 
		
		dist = new Distributor();
		dist.setHzInstance(hz);
		
		replicaresult = new ReplicationResult();
		replicaresult.setHzInstance(hz);
		
		coordinator = new MapCoordinator();
		coordinator.setHzInstance(hz);
		
		//task = new MapTask();
		//task.setHzInstance(hz);
		
		replicatask = new ReplicationTask();
		replicatask.setHzInstance(hz);
		
		mapStateResult = new MapStateResult();
		mapStateResult.setHzInstance(hz);
		mapStateResult.setMapStateResult();
		
		clientParams = In;
		
	}
	
	public void run(){
		/*
		 * Get rate limit status of application
		 */
		//downloader.getRateLimit();
		try {
			synchronized (this){
				while(suspended){
					wait();			
				}
			}
			receiverParams();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public synchronized void suspendThread(){
		suspended = true;
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : TASK SUSPEND!");
		System.out.println("");
		System.out.println("==============================================================================");
	}
	
	public synchronized void resumeThread(){
		suspended = false;
		notify();
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : TASK RESUME!");
		System.out.println("");
		System.out.println("==============================================================================");
	}
	
	/*
	 * Method setHz
	 * set hazelcast Instance
	 */
	private void setHz(){
		hz = cl.getHzInstance();
	}
	
	private void receiverParams(){
		String tm = cl.getThisMachine();
		String coord = cl.getCoordinator();
		
		// call mapResultToClient -> mapResult
		mapResultToClient = hz.getMap("mapResult");
		mapResultToClient.clear();// clear map result client
		
		//set state start
		mapStateResult.setStateStart();
		
		stringIn = clientParams;
		
		// get status from parameter or result search
		String status = getStatusStringIn(stringIn);
		
		// get worker's address "from_address"
		String fromaddress = getWorkerAddress(stringIn);
		
		int s = 1;
		if(status.equals("fromuser")){
			s = 1;
			// get keyword of search
			ks = getKeywordSearch(stringIn);
		}else if(status.equals("fromcoordinator")){
			s = 2;
		}else if(status.equals("result")){
			s = 3;
		}else if(status.equals("replicationresult")){
			s = 4;
			//keywordresult = getKeywordSearch(stringIn);
			nameFileObject = getObjectNameFile(stringIn);
		}else if(status.equals("replicationtask")){
			s = 5;
		}
		
		// add state to map 
		//task.addStateToMap();
		
		mapResultTemp = hz.getMap("mapResultTemp");
		
		switch(s){
			case 1: 
				/*
				 * if coordinator receives parameters from user
				 */
				if(tm.equals(coord)){
					// set state process
					mapStateResult.setStateProcess();
					
					// go to distributor job
					setFromCoordinator();
					//System.out.println(getStatus());
					String po = paramsOut(stringIn, getStatus());
					//System.out.println("parameters out : ");
					//System.out.println(po);
					/*
					 * task replication
					 */
					// set member
					replicatask.setMembers();
					// add task
					replicatask.addMapTask(po);
					//save task to this machine
					replicatask.saveReplicationTask(replicatask.getMapTask()); 
					/*
					 * send replication of task to members
					 */
					//System.out.println("member size:" +replicatask.getSizeMembers());
					try {
						for(int i=0; i<replicatask.getSizeMembers(); i++){
							destinationAddress = replicatask.queueMembersAddress().poll();
							replicatask.sendToQueue(destinationAddress, JSONObject.toJSONString(replicatask.getMapTask()));
						}
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// add task to map  
					//task.addMapTask(stringIn);
					
					System.out.println("");
					downloader.getRateLimit();
					//
					System.out.println("");
					System.out.println("==============================================================================");
					System.out.println("");
					System.out.println("GRID INFO : kirimkan parameter ke distributor task");
					System.out.println("");
					System.out.println("==============================================================================");
					System.out.println("");
					
					//paramsIn = po;
					
					// Coordinator search tweets
					downloader.crawlingProcess(stringIn);
					dist.setResultCoordinator(downloader.getSearch());
					System.out.println("==============================================================================");
					System.out.println("");
					System.out.println("GRID INFO : DOWNLOADER -> proses crawling selesai.");
					System.out.println("");
					System.out.println("==============================================================================");
					
					//System.out.println("Get Search Result: " + dist.getResultCoordinator());
					
					//reset resultIn counter
					resetResultIn();
					//set / increment resultIn counter
					setResultIn();
					//System.out.println("resultIn : " + getResultIn());
					
					// ambil statuses JSONArray
					gsr = getStatusesResult(dist.getResultCoordinator());
					if(gsr == null){
						gsrLength = 0;
					}else{
						// get length of coordinator's result
						gsrLength = gsr.length();
					}
					
					if(gsrLength <= 2){
						System.out.println("CRAWLING INFO : NO RESULTS");
						System.out.println("==============================================================================");
						
						// set state process
						mapStateResult.setStateNoResult();
						
						replicatask.clearMapTask();
						//task.clearMapTask();
						//MapTask mt = new MapTask();
						//mt.clearMapTask();
					}else{
						// substring gsr
						statuses = gsr.substring(1, gsrLength-1);
						// add result string to resultSearch StringBuilder
						//resultSearch = new StringBuilder(statuses);
						//resultSearch.append(statuses);
						
						mapResultTemp.put(1, statuses);
						
						// get max id from coordinator search result
						// System.out.println("(1) max id : " + getMaxId(dist.getResultCoordinator()));
						maxId = getMaxId(dist.getResultCoordinator());
						dist.setMaxID(maxId);
						
						boolean unreachable = true;
						// set max id to params
						//paramsWithMaxId = setMaxIdToParams(po, maxId);
						// poll 1st address from member's queue
						pollAddress = dist.queueMembersAddress().poll();
						
						String maxIdSend = setMaxIdSend(maxId);
						while(unreachable){
							try {
								// Distributing parameters to member
								// GANTI HANYA MENGIRIMKAN MAX ID
								//dist.sendToQueue(pollAddress, paramsWithMaxId);
								dist.sendToQueue(pollAddress, maxIdSend);
								unreachable = false;
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								System.out.println("GRID INFO : DISTRIBUTOR ~ " + pollAddress + "is unreachable");
								pollAddress = dist.queueMembersAddress().poll();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						/*try {
							// Distributing parameters to member
							// GANTI HANYA MENGIRIMKAN MAX ID
							//dist.sendToQueue(pollAddress, paramsWithMaxId);
							dist.sendToQueue(pollAddress, maxIdSend);
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("GRID INFO : DISTRIBUTOR ~ " + pollAddress + "is unreachable");
							pollAddress = dist.queueMembersAddress().poll();
							dist.sendToQueue(pollAddress, maxIdSend);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}
				}else{
					/*
					 * if member/worker receives parameters from user
					 * member/worker sends params to coordinator
					 */
					setFromMember();
					String po = paramsOut(stringIn, getStatus());
					clSend = new ClientSender(po);
					clSend.start();
					System.out.println("");
					System.out.println("==============================================================================");
					System.out.println("");
					System.out.println("GRID INFO : ");
					System.out.println("bukan coordinator grid. kirim parameters ke coordinator");
					System.out.println("");
					System.out.println("==============================================================================");
					System.out.println("");
				}
				break;
			case 2:
				System.out.println("");
				downloader.getRateLimit();
				
				/*
				 * worker receive max id from coordinator
				 * worker read parameter from replication task
				 * combine parameter and max_id
				 */
				String maxIdIn = getMaxIdFromSender(stringIn);
				try {
					String getParam = getParameter();
					paramsWithMaxId = setMaxIdToParams(getParam, maxIdIn);
					
					/*
					 * if member/worker receives max_id from coordinator
					 * combine parameter replication task & max_id 
					 * worker search tweets
					 * go to downloader
					 */
					downloader.crawlingProcess(paramsWithMaxId);
					//System.out.println("Search Result : " + downloader.getSearch());
					System.out.println("");
					System.out.println("==============================================================================");
					System.out.println("");
					System.out.println("GRID INFO : WORKER ~ send back result to coordinator");
					System.out.println("");
					System.out.println("==============================================================================");
					
					// add status result object to JSON search result
					String resultBack = addResultStatus(downloader.getSearch()).toJSONString();
					// System.out.println(resultBack);
					// send back search result to coordinator
					sendResultBack(resultBack);
					
					replicatask.clearMapTask();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case 3: 
				/*
				 * if coordinator receives result back from worker OR 
				 * if status message = result 
				 */
				setResultIn();
				//System.out.println("resultIn : " + getResultIn());
				System.out.println("");
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO : COORDINATOR ~ terima hasil search dari : " + fromaddress);
				System.out.println("");
				System.out.println("==============================================================================");
				
				// ambil statuses JSONArray
				gsr = getStatusesResult(stringIn);
				// get length of worker's result
				gsrLength = gsr.length();
				
				// substring gsr
				statuses = gsr.substring(1, gsrLength-1);
				//resultSearch.append(statuses);
				
				mapResultTemp.put(mapResultTemp.size()+1, statuses);
				
				//appendSb("hasil dari worker");
				//System.out.println("sb: " + getSb());
				
				// append result from worker
				//String rs = getResultSearch().toString();
				String rs = null;
				Integer sizeM = dist.sizeMembersSorting();
				// get max id
				maxId = getMaxId(stringIn);
				
				String maxIdSend = setMaxIdSend(maxId);
				
				if(maxId != null){ // check maxid for next crawling
					if(getResultIn() < sizeM){
						try {
							//System.out.println("(4) max id : " + getMaxId(stringIn));
							//maxId = getMaxId(stringIn);
							dist.setMaxID(maxId);
							// System.out.println("size member (from distributor): "+ sizeM);
							// set max id to params
							//paramsWithMaxId = setMaxIdToParams(paramsIn, maxId);
							
							pollAddress = dist.queueMembersAddress().poll();
							dist.sendToQueue(pollAddress, maxIdSend);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						setNameFile(ks, getDateNow()); // set name of file result
						String nf = getNameFile(); // get name of file result
						
						int c = 1;
						
						for (Object key : mapResultTemp.keySet()) {
							//System.out.println("Key : " + key + " Value : " + mapResultTemp.get(key));
							resultSearch.append(mapResultTemp.get(key));
							if(c != mapResultTemp.size()){
								resultSearch.append(",");
							}
							c++;
						}
						
						rs = resultSearch.toString();
						// SAVE RESULT OF SEARCH
						saveSearch(nf, "{\"keyword\": \""+ks+"\", \"statuses\":["+ rs + "]}");
						pathResultforCLient = downloader.getPathResult();
						System.out.println("result : " + pathResultforCLient);
						mapResultToClient.put("result", pathResultforCLient);
						
						mapResultTemp.clear();
						
						replicatask.clearMapTask();
						//clearing map of task
						//task.clearMapTask();
						
						// set state success
						mapStateResult.setStateSuccess();
					
						/*
						 * REPLICATION OF RESULT
						 * -> send replica of result to selected member
						 * -> create mapping of replication result
						 * -> create replica of mapping result and 
						 */
						replicaOfResult = "{\"status\": \"replicationresult\", \"namefile\": \""+ nf +"\", \"keyword\": \""+ks+"\", \"statuses\":["+ rs + "]}";
						// poll 1st address from member's queue
						pollAddress = replicaresult.queueMembersAddress().poll();
						
						try {
							/*
							 * create replica of result to 1 node
							 */
							replicaresult.setNameFile("result_"+ nf);
							
							// send replica of result to queue address member
							replicaresult.sendToQueue(pollAddress, replicaOfResult);
							
							// save mapping of address result to multimapresult
							replicaresult.setMultiMapResult(nf+".json", replicaresult.getThisMachine());
							replicaresult.setMultiMapResult(nf+".json", pollAddress);
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					setNameFile(ks, getDateNow());
					String nf = getNameFile();
					
					int c = 1;
					
					for (Object key : mapResultTemp.keySet()) {
						//System.out.println("Key : " + key + " Value : " + mapResultTemp.get(key));
						resultSearch.append(mapResultTemp.get(key));
						if(c != mapResultTemp.size()){
							resultSearch.append(",");
						}
						c++;
					}
					
					rs = resultSearch.toString();
					
					// SAVE RESULTS OF SEARCH
					saveSearch(nf, "{\"keyword\": \""+ ks +"\", \"statuses\":["+ rs + "]}");
					pathResultforCLient = downloader.getPathResult();
					System.out.println("result : " + pathResultforCLient);
					mapResultToClient.put("result", pathResultforCLient);
					
					mapResultTemp.clear();
					
					replicatask.clearMapTask();
					//clearing map of task
					//task.clearMapTask(); 
					
					// set state success
					mapStateResult.setStateSuccess();

					/*
					 * REPLICATION OF RESULT
					 * -> send replica of result to selected member
					 * -> create mapping of replication result
					 * -> create replica of mapping result and 
					 */
					replicaOfResult = "{\"status\": \"replicationresult\", \"namefile\": \""+ nf +"\", \"keyword\": \""+ks+"\", \"statuses\":["+ rs + "]}";
					// poll 1st address from member's queue
					pollAddress = replicaresult.queueMembersAddress().poll();
					
					try {
						/*
						 * create replica of result to 1 node
						 */
						replicaresult.setNameFile("result_"+ nf);
						
						// send replica of result to queue address member
						replicaresult.sendToQueue(pollAddress, replicaOfResult);
						
						// save mapping of address result to multimapresult
						replicaresult.setMultiMapResult(nf+".json", replicaresult.getThisMachine());
						replicaresult.setMultiMapResult(nf+".json", pollAddress);
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case 4: 
				/*
				 * if replicationresult send from coordinator
				 * step: 
				 *  -> receive result replica from coordinator
				 *  -> save to JSON
				 */
				//System.out.println("replication result in : "+ status);
				
				replicaresult.saveReplicaResult(nameFileObject, stringIn);
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO : REPLIKASI -> replikasi telah disimpan.");
				System.out.println("");
				System.out.println("==============================================================================");
				
				break;
			case 5:
				/*
				 * if replicationtask send from coordinator machine
				 */
				replicatask.taskInput(stringIn);
				/*
				 * perbaiki parsing string Input from coordinator
				 */
				//System.out.println("String In: ");
				//System.out.println(stringIn);
				replicatask.saveReplicationTask(replicatask.getMapTaskInput());
				
				System.out.println("==============================================================================");
				System.out.println("");
				System.out.println("GRID INFO : REPLIKASI TASK -> replikasi task telah disimpan.");
				System.out.println("");
				System.out.println("==============================================================================");
				
				//replicatask.clearMapTask();
				break;
		}
		/*
		 * buat code yang membersihkan task setelah pekerjaan selesai dilakukan
		 */
		
		
		coordinator.checkMapCoordinator();
		coordinator.showCoordinator();
		member.gridMembers();
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO: CRAWLER [MENUNGGU TASK]");
		System.out.println("");
		System.out.println("==============================================================================");
	}
	
	/*
	 * Method setResultIn
	 * increment counter of result in
	 */
	private void setResultIn(){
		resultIn += 1;
	}
	
	/*
	 * Method getResultIn
	 * return counter of result in
	 */
	private Integer getResultIn(){
		return resultIn;
	}
	
	/*
	 * Method resetResultIn
	 * reset counter of result in
	 */
	public void resetResultIn(){
		resultIn = 0;
	}
	
	/*
	 * Method setFromMember
	 * set status "frommember"
	 */
	private void setFromMember(){
		status = "frommember";
	}
	
	/*
	 * Method setFromCoordinator
	 * set status "fromcoordinator"
	 */
	private void setFromCoordinator(){
		status = "fromcoordinator";
	}
	
	/*
	 * Method getStatus
	 * get status of parameters
	 */
	private String getStatus(){
		return status;
	}
	
	/*
	 * Method getDateNow
	 * get date and time now
	 */
	private String getDateNow(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String dateSimpan = dateFormat.format(date);
		return dateSimpan;
	}
	
	/*
	 * Method setNameFile
	 * set name of file result of search
	 */
	private void setNameFile(String ks, String ds){
		nameFile = ks+"_"+ds;
	}
	
	/*
	 * Method getNameFile
	 * get name of file result of search
	 */
	private String getNameFile(){
		return nameFile;
	}
	
	/*
	 * Method saveSearch
	 * save result of search
	 */
	private void saveSearch(String namefile, String statuses){
		// save result
		Downloader.saveResult(namefile, statuses);
		System.out.println("");
		System.out.println("==============================================================================");
		System.out.println("");
		System.out.println("GRID INFO : HASIL SEARCH TELAH DISIMPAN!");
		System.out.println("");
		System.out.println("==============================================================================");
	}

	/*
	 * Method getStatusParamsIn
	 * get the status of parameters that receive from client
	 */
	private String getStatusStringIn(String paramsFromClient){
		JSONObject objparam = (JSONObject)JSONValue.parse(paramsFromClient);
		String statusParams = (String)objparam.get("status");
		return statusParams;
	}
	
	/*
	 * Method getKeywordSearch
	 * get keyword / q / query of Search
	 */
	private String getKeywordSearch(String paramsFromClient){
		JSONObject objparam = (JSONObject)JSONValue.parse(paramsFromClient);
		String keyword = (String)objparam.get("q");
		return keyword;
	}
	
	/*
	 * Method getObjectNameFile
	 * get name file object from json string input
	 */
	private String getObjectNameFile(String stringin){
		JSONObject objparam = (JSONObject)JSONValue.parse(stringin);
		String objectnamefile = (String)objparam.get("namefile");
		return objectnamefile;
	}
	
	/*
	 * Method getWorkerAddress
	 * get the status of parameters that receive from client
	 */
	private String getWorkerAddress(String resultfromworker){
		JSONObject objparam = (JSONObject)JSONValue.parse(resultfromworker);
		String workerAddress = (String)objparam.get("from_address");
		return workerAddress;
	}
	
	
	/*
	 * Method paramsOut
	 * receive parameters in & change status of parameters
	 */
	private String paramsOut(String paramsFromClient, String statusparams){
		String jsonMap = null;
		JSONObject objparam = (JSONObject)JSONValue.parse(paramsFromClient);
		//String status = (String)objparam.get("status");
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
		mapParams.put("status", statusparams);
		mapParams.put("q", q);
		mapParams.put("geocode", geocode);
		mapParams.put("lang", lang);
		mapParams.put("locale", locale);
		mapParams.put("result_type", result_type);
		mapParams.put("count", count);
		mapParams.put("until", until);
		mapParams.put("since_id", since_id);
		mapParams.put("max_id", max_id);
		mapParams.put("include_entities", include_entities);
		mapParams.put("callback", callback);
		jsonMap = JSONObject.toJSONString(mapParams);
		return jsonMap;
	}
	
	
	/*
	 * Method sendResultBack
	 * send result of search back to coordinator
	 */
	private void sendResultBack(String resultBack){
		String coord = cl.getCoordinator();
		
		try {
			coordSocket = new Socket(coord, coordPort);
			outStreamWrite = new OutputStreamWriter(coordSocket.getOutputStream(), "UTF-8");
			outStreamWrite.write(resultBack,0, resultBack.length());
			outStreamWrite.close();
			coordSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Method setMaxIdToParams
	 * set max_id to parameters
	 */
	private String setMaxIdToParams(String paramsOut, String max_id){
		String jsonMap = null;
		JSONObject objparam = (JSONObject)JSONValue.parse(paramsOut);
		String status = (String)objparam.get("status");
		String q = (String)objparam.get("q");
		String geocode = (String)objparam.get("geocode");
		String lang = (String)objparam.get("lang");
		String locale = (String)objparam.get("locale");
		String result_type = (String)objparam.get("result_type");
		String count = (String)objparam.get("count");
		String until = (String)objparam.get("until");
		String since_id = (String)objparam.get("since_id");
		//String max_id = (String)objparam.get("max_id");
		String include_entities = (String)objparam.get("include_entities");
		String callback = (String)objparam.get("callback");
		mapParams.put("status", status);
		mapParams.put("q", q);
		mapParams.put("geocode", geocode);
		mapParams.put("lang", lang);
		mapParams.put("locale", locale);
		mapParams.put("result_type", result_type);
		mapParams.put("count", count);
		mapParams.put("until", until);
		mapParams.put("since_id", since_id);
		mapParams.put("max_id", max_id);
		mapParams.put("include_entities", include_entities);
		mapParams.put("callback", callback);
		jsonMap = JSONObject.toJSONString(mapParams);
		return jsonMap;
	}
	
	/*
	 * Method setMaxIdSend
	 * set max_id send from coordinator to worker
	 */
	private String setMaxIdSend(String max_id){
		String jsonMap = null;
		Map<String, String> mapMaxId = new HashMap<String, String>();
		mapMaxId.put("status", "fromcoordinator");
		mapMaxId.put("max_id", max_id);
		jsonMap = JSONObject.toJSONString(mapMaxId);
		return jsonMap;
	}
	
	/*
	 * Method getMaxIdFromSender
	 * get max id that send from coordinator
	 */
	private String getMaxIdFromSender(String StringIn){
		JSONObject objparam = (JSONObject)JSONValue.parse(StringIn);
		String max_id = (String)objparam.get("max_id");
		return max_id;
	}
	
	/*
	 * Method getParameter
	 * get parameter from replication task
	 */
	private String getParameter() throws FileNotFoundException{
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
	 * Method getMaxId
	 * get max id for next result max id parameter
	 */
	private String getMaxId(String resultFromMember){
		JSONObject objResult = (JSONObject)JSONValue.parse(resultFromMember);
		JSONObject sm = (JSONObject)objResult.get("search_metadata");
		String nr = (String)sm.get("next_results");
		if(nr != null){
			String delim = "[?&=]";
			String[] max = nr.split(delim);
			return max[2];
		}else{
			return null;
		}
	}
	
	/*
	 * Method addResultStatus
	 * add status object result to JSON result search
	 */
	@SuppressWarnings("unchecked")
	private JSONObject addResultStatus(String resultFromMember){
		JSONObject objResult = (JSONObject)JSONValue.parse(resultFromMember);
		// menambahkan status object pada json yang sudah ada
		JSONObject objStatus = new JSONObject();
		objStatus.put("status", "result");
		objStatus.put("from_address", dist.getThisMachine());
		objResult.putAll(objStatus);
		return objResult;
	}
	
	/*
	 * Method getStatusesResult
	 * get statuses JSONArray from search result
	 */
	private String getStatusesResult(String result){
		// ekstrak json array dari json object
		JSONObject objResult = (JSONObject)JSONValue.parse(result);
		JSONArray arrayStatuses = (JSONArray)objResult.get("statuses");
		if(arrayStatuses.size()==0){
			return null;
		}else{
			String arrayString = arrayStatuses.toJSONString();
			return arrayString;
		}
	}
	
	/*
	 * Method getStatusesResult
	 * get statuses JSONArray from search result
	 */
	public String getArrayJSON(String result){
		// ekstrak json array dari json object
		JSONObject objResult = (JSONObject)JSONValue.parse(result);
		if(objResult.isEmpty()){
			return null;
		}else{
			JSONArray arrayStatuses = (JSONArray)objResult.get("statuses");
			String arrayString = arrayStatuses.toJSONString();
			return arrayString;
		}
	}
	
	/*
	 * Method getStatusesResult
	 * get statuses JSONArray from search result
	 */
	public String getObjectJSON(String result){
		// ekstrak json array dari json object
		JSONObject objResult = (JSONObject)JSONValue.parse(result);
		if(objResult.isEmpty()){
			return null;
		}else{
			return objResult.toString();
		}
	}
	
}
