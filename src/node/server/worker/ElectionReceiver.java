/**
 * 
 */
package node.server.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
//import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.IMap;

import node.server.coordinator.MapMember;
import node.server.main.DirectoryHandler;
import node.server.worker.Election;

/**
 * @author Willi
 * email : willi.ilmukomputer@gmail.com
 */

public class ElectionReceiver extends Thread{

	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private Election el;
	private ElectionForwarder elForward;
	private MapMember mm;
	private MapCoordinator mc;
	private static HazelcastInstance hz;
	private String thisMachine = null;
	private static ServerSocket server = null;
	private static Socket client = null;
	private static Integer electionPort = 4444;
	private InputStream in;
	//private Map<Integer, String> mapSorting; 
	private Map<Integer, String> mapElectionTemp;
	//private IMap<String, String> mapCoordinator;
	
	/*
	 * Method ElectionReceiver
	 */
	public ElectionReceiver(){
		el = new Election();
		setHz();
		mm = new MapMember();
		mc = new MapCoordinator();
		mm.setHzInstance(hz);
		mc.setHzInstance(hz);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		receiveElectionMessage();	
		
		el.resetCounterMessage();
		mm.gridMembers();
		System.out.println("==============================================================================");
		System.out.println("");
		mc.showCoordinator();
		System.out.println("==============================================================================");
		System.out.println("");
		
		
		//closeServer();
		// coba nanti ubah, ganti dengan cara memanggil closeServer() dari mapCoordinator updated entry listener
		// catatan: lihat file CATATAN_ELECTION_ALGORITMA
	}
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHz(){
		hz = el.getHzInstance();
	}
	
	/*
	 * Method closeServer
	 * close socket of server, client, and inputstream.
	 */
	public void closeServer(){
		try {
			in.close();
			client.close();
			server.close();
			System.out.println("GRID INFO: ELECTION -> socket closed! ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Method receiveElectionMessage
	 * receive election message from another members of Grid
	 */
	private void receiveElectionMessage(){
		/*
		 * if localmember equals with one member of election message, end sending election message,
		 * do electing coordinator from election message
		 */
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
		try {
			server = new ServerSocket(electionPort); // open port for accepting 
			
			//while(true){
			while(el.getCounterMessage() <= el.getSizeCluster()){
				System.out.println("GRID INFO: ELECTION [RECEIVE MESSAGE]");
				System.out.println(dt.format(cal.getTime()) +" -> [terima] node mempersiapkan menerima election message");
				System.out.println("==============================================================================");
				System.out.println("");
				
				client = server.accept();
				System.out.println("GRID INFO: ELECTION [RECEIVE MESSAGE]");
				System.out.println(dt.format(cal.getTime()) +" -> [terima] node menerima election message");
				el.setCounterMessage();
				System.out.println("counter message " + el.getCounterMessage());
				System.out.println("==============================================================================");
				System.out.println("");
				
				in = client.getInputStream();
				String electionMessage = getStringFromInputStream(in); 
				String inetFirst = el.firstAddress(electionMessage);
				matchAddress(inetFirst, electionMessage);
			}
			el.resetCounterMessage();
			closeServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Method getStringFromInputStream
	 * get string format from inputstream
	 */
	private String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	/*
	 * Method matchAddress
	 * matching the local address with 1st member in election message.
	 */
	private void matchAddress(String inetFirst, String electionMessage) throws ParseException{
		// if thisMachine equals 1 address or the first address from mapElection (election message), doElection
		// else forwardNextAddress
		
		//thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		thisMachine = el.getThisMachine();
		String[] splitAddress = inetFirst.split("-");
		String addrMachine = splitAddress[1];
		if(thisMachine.equals(addrMachine)){
			// do election
			System.out.println("GRID INFO: ELECTION ~ GRID ELECTION COORDINATOR : ");
			System.out.println("	this machine's address equals");
			System.out.println("	status : OK");
			System.out.println("==============================================================================");
			System.out.println("");
			el.doElection(electionMessage);
		}else{
			try {
				String electionMessageForward = null;
				electionMessageForward = addElectionMessage(electionMessage);
				
				System.out.println("GRID INFO: ELECTION ~ GRID ELECTION COORDINATOR : ");
				System.out.println("	this machine's address not equals");
				
				// forwarding election message to next address
				System.out.println("    	adding this machine address to election message");
				System.out.println("==============================================================================");
				System.out.println("");
				elForward = new ElectionForwarder(electionMessageForward);
				elForward.start();
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Method addElectionMessage
	 * add this machine address to election message
	 */
	private String addElectionMessage(String electionMessage) throws ParseException{
		String jsonMapElection = null;
		mapElectionTemp = new HashMap<Integer, String>();
		//thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		thisMachine = el.getThisMachine();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(electionMessage);
		JSONObject jObj = (JSONObject)obj;
		int sizeMessage = jObj.size();
		for(Integer i=1; i<=sizeMessage; i++){
			String num = i.toString();
			String addr = (String)jObj.get(num);
			mapElectionTemp.put(i,addr);
		}
		int sizeMapTemp = mapElectionTemp.size();
		
		File file = new File(dirHandler.getDirMember()+"./member.json");
		Boolean fileExist = file.exists();
		if(fileExist == true){
			try {
				JSONObject objmember = (JSONObject)JSONValue.parse(new FileReader(dirHandler.getDirMember()+"./member.json"));
				JSONArray arrMember = (JSONArray)objmember.get("member");
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
							mapElectionTemp.put(sizeMapTemp+1, key+"-"+value);
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		//mapElectionTemp.put(sizeMapTemp+1, thisMachine);
		//mapSorting = new TreeMap<Integer, String>(mapElectionTemp);
		//String jsonMapElection = JSONObject.toJSONString(mapSorting);
		jsonMapElection = JSONObject.toJSONString(mapElectionTemp);
		mapElectionTemp.clear();
		//mapSorting.clear();
		return jsonMapElection;
	}
	
}
