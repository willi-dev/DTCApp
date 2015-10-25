/**
 * 
 */
package node.server.coordinator;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Willi
 * willi.ilmukomputer@gmail.com
 */
public class MapStateResult implements EntryListener<String, String>{

	private static HazelcastInstance hz;
	private IMap<String, String> mapStateResult;
	private String mr = "mapstateresult";
	
	public void setHzInstance(HazelcastInstance hi){
		hz = hi;
	}
	
	public void setMapStateResult(){
		mapStateResult = hz.getMap(mr);
	}
	
	public void setStateStart(){
		setMapStateResult();
		mapStateResult.put("result", "start");
	}
	
	public void setStateProcess(){
		setMapStateResult();
		mapStateResult.put("result", "process");
	}
	
	public void setStateNoResult(){
		setMapStateResult();
		mapStateResult.put("result", "noresult");
	}
	
	public void setStateSuccess(){
		setMapStateResult();
		mapStateResult.put("result", "success");
	}

	@Override
	public void entryAdded(EntryEvent<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryEvicted(EntryEvent<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryRemoved(EntryEvent<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryUpdated(EntryEvent<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}

