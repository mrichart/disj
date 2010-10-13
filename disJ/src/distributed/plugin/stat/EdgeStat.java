package distributed.plugin.stat;

import java.util.HashMap;
import java.util.Map;

public class EdgeStat extends Statistic {

	private int totalMsgEnter;
	private int totalMsgLeave;
	
	// total time of msg stay in this link
	private int totalTimeUse; 
	
	private String edgeId;
	
	// number of time of different type of msg using this link
	private Map<String, Integer> msgTypes; 
	
	public EdgeStat(String name) {
		this.edgeId = name;
		this.totalMsgEnter = 0;
		this.totalMsgLeave = 0;
		this.totalTimeUse = 0;
		
		this.msgTypes = new HashMap<String, Integer>();
	}

	@Override
	public void reset() {
		this.totalMsgEnter = 0;
		this.totalMsgLeave = 0;
		this.totalTimeUse = 0;
		
		this.msgTypes = new HashMap<String, Integer>();		
	}

	@Override
	public String getName() {
		return this.edgeId;
	}

	public int getTotalMsgEnter() {
		return totalMsgEnter;
	}

	public void incMsgEnter(){
		this.totalMsgEnter++;
	}
	
	public int getTotalMsgLeave() {
		return totalMsgLeave;
	}

	public void incMsgLeave(){
		this.totalMsgLeave++;
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
		return msgTypes;
	}
	
	public void incMsgCount(String msgType){
		if(this.msgTypes.containsKey(msgType)){
			int count = this.msgTypes.get(msgType);
			count++;
			this.msgTypes.put(msgType, count);			
		}else{
			this.msgTypes.put(msgType, 1);
		}
	}
	

}
