package distributed.plugin.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.StringTokenizer;

import distributed.plugin.runtime.engine.TimeGenerator;

public class Logger {
	
	public enum logTag{ 
		NODE_STATE, NODE_SEND, NODE_INIT, NODE_RECV, EDGE_MSG, 
		EDGE_LOST, AGENT_STATE, AGENT_DIE, AGENT_LEAVE, AGENT_ARRIVE,
		AGENT_WRITE_TO_BOARD, AGENT_DELETE_FROM_BOARD, AGENT_DROP_TOKEN,
		AGENT_PICK_TOKEN, AGENT_AWAKE, AGENT_SLEEP, AGENT_NOTIFY
	}
	
	private String graphId;
	
	private URL dirUrl;
	
	private PrintWriter out;
	
	private TimeGenerator timeGen;
	
	public Logger (String graphId, URL dirUrl, TimeGenerator timeGen){
		if(graphId == null || dirUrl == null || timeGen == null){
			throw new IllegalArgumentException("@Logger.constructor Parameters cannot be null");
		}
		this.graphId = graphId;
		this.dirUrl = dirUrl;
		this.out = null;
		this.timeGen = timeGen;
	}
	
	public void initLog() throws IOException{
		if(out == null){
			File dir = new File(this.dirUrl.getPath());
			StringTokenizer tok = new StringTokenizer(this.graphId, ".");
			String name = tok.nextToken();
			File r = new File(dir, name + ".rec");
			this.out = new PrintWriter(new FileWriter(r));
		}
	}
	
	public void cleanUp(){
		if(this.out != null){
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}
	
	private int getCurrentTime() {
		return this.timeGen.getCurrentTime(this.graphId);
	}
	
	public void logNode(logTag tag, String nodeId, Object value){
		this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId + "," + value);
	}
	
	public void logEdge(logTag tag, String edgeId, Object value) {
		this.out.println(this.getCurrentTime()+ "," + tag + "," + edgeId + "," + value);
	}
	
	public void logAgent(logTag tag, String agentId, Object value){
		this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," + value);
	}
	
	

}
