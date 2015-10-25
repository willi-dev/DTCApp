package node.server.main;

import java.io.File;

public class DirectoryHandler {

	private String pathExecJar = null;
	private String pathDirParams = null; // for client
	private String pathDirToken = null; // for client and server cluster
	private String pathDirResults = null; // for server cluster store a results of crawl
	private String pathDirMember = null; // for server cluster
	private String pathDirWorker = null; // for server cluster
	private String pathDirCoordinator = null; // for election
	private String pathDirReplication = null; // for replication
	
	public DirectoryHandler(){
		pathExecJar = MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
	
	public String getDirParams(){
		pathDirParams = new File(pathExecJar).getParentFile().getPath()+"/params";
		return pathDirParams;
	}
	
	public String getDirToken(){
		pathDirToken = new File(pathExecJar).getParentFile().getPath()+"/token";
		return pathDirToken;
	}
	
	public String getDirResult(){
		pathDirResults = new File(pathExecJar).getParentFile().getPath()+"/results";
		return pathDirResults;
	}
	
	public String getDirMember(){
		pathDirMember = new File(pathExecJar).getParentFile().getPath()+"/member";
		return pathDirMember;
	}
	
	public String getDirWorker(){
		pathDirWorker = new File(pathExecJar).getParentFile().getPath()+"/worker";
		return pathDirWorker;
	}
	
	public String getDirCoordinator(){
		pathDirCoordinator = new File(pathExecJar).getParentFile().getPath()+"/coordinator";
		return pathDirCoordinator;
	}
	
	public String getDirReplication(){
		pathDirReplication = new File(pathExecJar).getParentFile().getPath()+"/replication";
		return pathDirReplication;
	}
	
}
