package distributed.plugin.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import distributed.plugin.runtime.engine.TimeGenerator;

public class Logger {
	
	public enum logTag{ 
		NODE_STATE, NODE_SEND, NODE_INIT, NODE_RECV, NODE_DIE, EDGE_MSG, 
		EDGE_LOST, AGENT_STATE, AGENT_DIE, AGENT_LEAVE, AGENT_ARRIVE,
		AGENT_WRITE_TO_BOARD, AGENT_DELETE_FROM_BOARD, AGENT_DROP_TOKEN,
		AGENT_PICK_TOKEN, AGENT_AWAKE, AGENT_SLEEP, AGENT_NOTIFY,
		AGENT_INIT, STATE_FIELD, MODEL_MSG_PASS, MODEL_AGENT_TOKEN, 
		MODEL_AGENT_BOARD
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
			if(r.exists()){
				r.delete();
			}
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
	
	/**
	 * Log state list into a line of string
	 * 
	 * @param tag A logTag for state list record
	 * @param states A map of state list 
	 */
	public void logStates(logTag tag, Map<Integer, String> states){
		Iterator<Integer> its = states.keySet().iterator();
		String name = null;
		boolean first = true;
		StringBuffer buff = new StringBuffer();
		for(Integer i = 0; its.hasNext();){
			i = its.next();
			name = states.get(i);
			if(first){
				buff.append(tag + "," +  i + ":" + name);
				first = false;
			}else{
				buff.append("," + i + ":" + name);
			}
		}
		this.out.println(buff.toString());	
		this.out.flush();
	}
	
	/**
	 * Log the type of model that is simulating
	 * 
	 * @param tag A tag corresponding to a model
	 * @param className A fully qualified java class name of user entity
	 */
	public void logModel(logTag tag, String className){
		this.out.println(this.getCurrentTime() + "," + tag + ","+ className);
		this.out.flush();
	}	
	
	public void logNode(logTag tag, String nodeId, Object value){
		
		 if(tag == logTag.NODE_STATE){
			String stateId = (String) value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId + "," 
					+ stateId);
			
		} else if (tag == logTag.NODE_RECV){
			String[] tmp = (String[]) value; // {from_port, msgLabel}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.NODE_SEND){
			String[] tmp = (String[]) value; // {to_port, msgLabel}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.NODE_DIE){
			this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId);
			
		} else if(tag == logTag.NODE_INIT){
			this.out.println(this.getCurrentTime() + "," + tag + ","+ nodeId);			
		}
		this.out.flush();
	}
	
	public void logEdge(logTag tag, String edgeId, Object value) {
		
		if(tag == logTag.EDGE_MSG || tag == logTag.EDGE_LOST){
			String[] tmp = (String[]) value; // {nodeId, msgLabel}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ edgeId + "," 
					+ tmp[0] + "," + tmp[1]);
		}
		this.out.flush();
	}
	
	public void logAgent(logTag tag, String agentId, Object value){
		
		if(tag == logTag.AGENT_STATE){
			String stateId = (String)value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ stateId);
			
		} else if (tag == logTag.AGENT_NOTIFY){
			String[] tmp = (String[]) value; // {nodeId, notify_type}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_LEAVE){
			String[] tmp = (String[]) value; // {nodeId, to_port}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if(tag == logTag.AGENT_ARRIVE){
			String[] tmp = (String[]) value; // {nodeId, from_port}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_AWAKE){
			String nodeId = (String)value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ nodeId);
			
		} else if (tag == logTag.AGENT_SLEEP) {
			String[] tmp = (String[]) value; // {nodeId, awake_time}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_DIE){
			String nodeId = (String)value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ nodeId);
			
		} else if (tag == logTag.AGENT_DELETE_FROM_BOARD){
			String[] tmp = (String[]) value; // {nodeId, info}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_WRITE_TO_BOARD){
			String[] tmp = (String[]) value; // {nodeId, info}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_DROP_TOKEN){
			String nodeId = (String)value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ nodeId);
			
		} else if (tag == logTag.AGENT_PICK_TOKEN){
			String[] tmp = (String[]) value; // {nodeId, amount_token_drop}			
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ tmp[0] + "," + tmp[1]);
			
		} else if (tag == logTag.AGENT_INIT){
			String hostId = (String)value;
			this.out.println(this.getCurrentTime() + "," + tag + ","+ agentId + "," 
					+ hostId);
		}
		this.out.flush();
	}
	
	/**
	 * Read state list from a line of log string
	 * 
	 * @param line First line of log record
	 * @return A map of state list
	 */
	public static Map<Integer, String> readStates(String line){
		String[] value = line.split(",");
		String[] pair = null;
		Map<Integer, String> tmp = new HashMap<Integer, String>();
		logTag tag = logTag.valueOf(value[0]);
		if(tag == logTag.STATE_FIELD){
			for(int i = 1; i < value.length; i++){
				pair = value[i].split(":");
				tmp.put(Integer.parseInt(pair[0]), pair[1]);				
			}
		}
		return tmp;
	}
	
	/**
	 * Read a log string and split into block of data
	 * 
	 * @param line A line of log string
	 * @return
	 */
	public static String[] readLogLine(String line){
		String[] value = line.split(",");
		return value;
	}		
	

}
