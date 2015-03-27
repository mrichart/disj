package distributed.plugin.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.IConstants;

@SuppressWarnings("serial")
public class NodeStat extends Statistic {
	
	private int numMsgRecv;
	private int numMsgSent;	
	private int numAgentVisit;
	private int numTokDrop;
	private int numTokPick;
	private int numBoardRead;
	private int numBoardWrite;
	private int numBoardDel;

	private String nodeId;	
	private List<String> pastStates;

	// number of message sent at different state
	private Map<Integer, Integer> stateMsgSent;
	
	/**
	 * 
	 * @param name
	 */
	public NodeStat(String name) {
		this.nodeId = name;
		this.numAgentVisit = 0;
		this.numMsgRecv = 0;
		this.numMsgSent = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		this.numBoardDel = 0;
		this.numBoardRead = 0;
		this.numBoardWrite = 0;
		
		this.pastStates = new ArrayList<String>();
		this.stateMsgSent = new HashMap<Integer, Integer>();
	}
	
	@Override
	public void reset(){
		this.numAgentVisit = 0;
		this.numMsgRecv = 0;
		this.numMsgSent = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		
		this.pastStates = new ArrayList<String>();
		this.stateMsgSent = new HashMap<Integer, Integer>();
	}

	@Override
	public String getName() {		
		return nodeId;
	}
	
	public int getNumMsgRecv() {
		return numMsgRecv;
	}

	public void incNumMsgRecv(){
		Integer old = this.numMsgRecv;
		this.numMsgRecv++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numMsgRecv);
	}
	
	public int getNumMsgSent() {
		return numMsgSent;
	}
	
	public void incNumMsgSent(){
		Integer old = this.numMsgSent;
		this.numMsgSent++;		
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numMsgSent);
	}
	
	public Map<Integer, Integer> getStateMsgSent() {
		return this.stateMsgSent;
	}
	
	public void incStateMsgSent(int stateId){
		if(this.stateMsgSent.containsKey(stateId)){
			int count = this.stateMsgSent.get(stateId);
			count++;
			this.stateMsgSent.put(stateId, count);
		}else{
			this.stateMsgSent.put(stateId, 1);
		}
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, null, this.stateMsgSent);
	}

	public int getNumAgentVisit() {
		return numAgentVisit;
	}

	public void incNumAgentVisit(){
		Integer old = this.numAgentVisit;
		this.numAgentVisit++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numAgentVisit);
	}
	
	public int getNumTokDrop() {
		return numTokDrop;
	}
	
	public void incNumTokDrop(){
		Integer old = this.numTokDrop;
		this.numTokDrop++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numTokDrop);
	}

	public int getNumTokPick() {
		return numTokPick;
	}

	public void incNumTokPick(){
		Integer old = this.numTokPick;
		this.numTokPick++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numTokPick);
	}
		
	public int getNumBoardRead() {
		return numBoardRead;
	}

	public void incNumBoardRead(){
		Integer old = this.numBoardRead;
		this.numBoardRead++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numBoardRead);
	}
	
	public int getNumBoardWrite() {
		return numBoardWrite;
	}

	public void incNumBoardWrite(){
		Integer old = this.numBoardWrite;
		this.numBoardWrite++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numBoardWrite);
	}
	
	public int getNumBoardDel() {
		return numBoardDel;
	}

	public void incNumBoardDel(){
		Integer old = this.numBoardDel;
		this.numBoardDel++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numBoardDel);
	}
	
	public List<String> getPastStates() {
		return pastStates;
	}
	
	public void addPastState(String state){
		this.pastStates.add(state);
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, null, this.pastStates);
	}

	public void clearPastState(){
		this.pastStates.clear();
	}
}
