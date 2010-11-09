package distributed.plugin.stat;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class EdgeStat extends Statistic {

	private int totalEnter;
	private int totalLeave;
	
	// total time of msg stay in this link
	private int totalTimeUse; 
	
	private String edgeId;
	
	// number of time of different type of msg using this link
	private Map<String, Integer> msgCreateTypes; 
	
	/**
	 * 
	 * @param name
	 */
	public EdgeStat(String name) {
		this.edgeId = name;
		this.totalEnter = 0;
		this.totalLeave = 0;
		this.totalTimeUse = 0;
		
		this.msgCreateTypes = new HashMap<String, Integer>();
	}

	@Override
	public void reset() {
		this.totalEnter = 0;
		this.totalLeave = 0;
		this.totalTimeUse = 0;
		
		this.msgCreateTypes = new HashMap<String, Integer>();		
	}

	@Override
	public String getName() {
		return this.edgeId;
	}

	public int getTotalEdgeEnter() {
		return totalEnter;
	}

	public void incEnterEdge(){
		this.totalEnter++;
	}
	
	public int getTotalEdgeLeave() {
		return totalLeave;
	}

	public void incLeaveEdge(){
		this.totalLeave++;
	}
	
	public int getTotalTimeUse() {
		return totalTimeUse;
	}

	public void addTimeUse(int time){
		if(time > 0){
			this.totalTimeUse += time;
		}
	}

	public Map<String, Integer> getMsgTypes() {
		return msgCreateTypes;
	}
	
	public void incMsgCreatCount(String msgType){
		if(this.msgCreateTypes.containsKey(msgType)){
			int count = this.msgCreateTypes.get(msgType);
			count++;
			this.msgCreateTypes.put(msgType, count);			
		}else{
			this.msgCreateTypes.put(msgType, 1);
		}
	}
	

}
