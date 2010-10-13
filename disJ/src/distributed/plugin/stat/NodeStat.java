package distributed.plugin.stat;

import java.util.ArrayList;
import java.util.List;

public class NodeStat extends Statistic {
	
	private int numMsgRecv;
	private int numMsgSend;	
	private int numAgentVisit;
	private int numTokDrop;
	private int numTokPick;

	private String nodeId;	
	private List<String> pastStates;
	
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
		this.numMsgRecv++;
	}
	
	public int getNumMsgSend() {
		return numMsgSend;
	}
	
	public void incNumMsgSend(){
		this.numMsgSend++;
	}

	public int getNumAgentVisit() {
		return numAgentVisit;
	}

	public void incNumAgentVisit(){
		this.numAgentVisit++;
	}
	
	public int getNumTokDrop() {
		return numTokDrop;
	}
	
	public void incNumTokDrop(){
		this.numTokDrop++;
	}

	public int getNumTokPick() {
		return numTokPick;
	}

	public void incNumTokPick(){
		this.numTokPick++;
	}
	
	public List<String> getPastStates() {
		return pastStates;
	}
	
	public void addState(String state){
		this.pastStates.add(state);
	}

	public void clearState(){
		this.pastStates.clear();
	}
}
