package distributed.plugin.stat;

import java.util.ArrayList;
import java.util.List;

import distributed.plugin.core.IConstants;

@SuppressWarnings("serial")
public class NodeStat extends Statistic {
	
	private int numMsgRecv;
	private int numMsgSend;	
	private int numAgentVisit;
	private int numTokDrop;
	private int numTokPick;

	private String nodeId;	
	private List<String> pastStates;

	/**
	 * 
	 * @param name
	 */
	public NodeStat(String name) {
		this.nodeId = name;
		this.numAgentVisit = 0;
		this.numMsgRecv = 0;
		this.numMsgSend = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		
		this.pastStates = new ArrayList<String>();
	}
	
	@Override
	public void reset(){
		this.numAgentVisit = 0;
		this.numMsgRecv = 0;
		this.numMsgSend = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		
		this.pastStates = new ArrayList<String>();
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
	
	public int getNumMsgSend() {
		return numMsgSend;
	}
	
	public void incNumMsgSend(){
		Integer old = this.numMsgSend;
		this.numMsgSend++;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_STATISTIC_NODE, old, this.numMsgSend);
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
