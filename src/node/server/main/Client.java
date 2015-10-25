/**
 * 
 */
package node.server.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author dell
 *
 */
public class Client {

	private static HazelcastInstance hz;
	private String thisMachine = null;
	private String nMap = "coordinator";
	private IMap<String, String> mapCoordinator;
	protected static Socket client;
	
	/*
	 * Method setClient
	 * set client
	 */
	public void setClient(Socket socket){
		client = socket;
	}
	
	/*
	 * Method getClient
	 * get client
	 */
	public Socket getClient(){
		return client;
	}
	
	/*
	 * Method setHzInstance
	 * set hazelcast instance from main.
	 */
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	/*
	 * Method getHzInstance
	 * get hazelcast instance from main
	 */
	public HazelcastInstance getHzInstance(){
		return hz;
	}
	
	/*
	 * Method getThisMachine
	 * return address of this machine
	 */
	public String getThisMachine(){
		thisMachine = hz.getCluster().getLocalMember().getInetSocketAddress().getAddress().toString().substring(1);
		return thisMachine;
	}
	
	/*
	 * Method getCoordinator
	 * return address of coordinator 
	 */
	public String getCoordinator(){
		mapCoordinator = hz.getMap(nMap);
		String getCoordinator = mapCoordinator.get("coordinator");
		return getCoordinator;
	}
	

	/*
	 * Method receiveParamsFromClient
	 * receiving parameters of search from client
	 */
	public String receiveStringFromClient() throws IOException{
		InputStream in = client.getInputStream();
		String paramsIn = getStringFromInputStream(in);
		return paramsIn;
	}
	
	/*
	 * Method getStringFromInputStream
	 * get string content from inputstream
	 */
	public static String getStringFromInputStream(InputStream is) {
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
	
	
}
