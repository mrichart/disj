package distributed.plugin.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentStat extends Statistic {

	private int numMove;
	private int numBoardRead;
	private int numBoardWrite;
	private int numBoardDel;
	
	private int numTokPick;
	private int numTokDrop;
		
	private String agentId;
	
	private List<String> pastStates;
	
	// number of time of different node that agent visited
	private Map<String, Integer> nodeVisit;

	// number of time that agent moves at different state
	private Map<Integer, Integer> stateMove;
	
	// number of time that agent pick token at different node
	private Map<String, Integer> tokPick;
	
	// number of time that agent drop token at different node
	private Map<String, Integer> tokDrop;
	
	/**
	 * 
	 * @param name
	 */
	public AgentStat(String name) {
		this.agentId = name;
		this.numBoardDel = 0;
		this.numBoardRead = 0;
		this.numBoardWrite = 0;
		this.numMove = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		
		this.pastStates = new ArrayList<String>();
		this.nodeVisit = new HashMap<String, Integer>();
		this.stateMove = new HashMap<Integer, Integer>();
		this.tokPick = new HashMap<String, Integer>();
		this.tokDrop = new HashMap<String, Integer>();
	}

	@Override
	public void reset() {
		this.numBoardDel = 0;
		this.numBoardRead = 0;
		this.numBoardWrite = 0;
		this.numMove = 0;
		this.numTokDrop = 0;
		this.numTokPick = 0;
		
		this.pastStates = new ArrayList<String>();
		this.nodeVisit = new HashMap<String, Integer>();
		this.stateMove = new HashMap<Integer, Integer>();
		this.tokPick = new HashMap<String, Integer>();
		this.tokDrop = new HashMap<String, Integer>();
	}

	@Override
	public String getName() {
		return this.agentId;
	}

	public int getNumMove() {
		return numMove;
	}
	
	public void incMove(){
		this.numMove++;
	}

	public int getNumBoardRead() {
		return numBoardRead;
	}
	
	public void incRead(){
		this.numBoardRead++;
	}

	public int getNumBoardWrite() {
		return numBoardWrite;
	}
	
	public void incWrite(){
		this.numBoardWrite++;
	}

	public int getNumBoardDel() {
		return numBoardDel;
	}

	public void incDelete(){
		this.numBoardDel++;
	}
	
	public int getNumTokPick() {
		return numTokPick;
	}

	public void incPick(){
		this.numTokPick++;
	}
	
	public int getNumTokDrop() {
		return numTokDrop;
	}

	public void incDrop(){
		this.numTokDrop++;
	}
	
	public List<String> getPastStates() {
		return pastStates;
	}
	
	public void addState(String state){
		this.pastStates.add(state);
	}

	public Map<String, Integer> getNodeVisit() {
		return nodeVisit;
	}
	
	public void incNodeVisit(String nodeId){
		if(this.nodeVisit.containsKey(nodeId)){
			int count = this.nodeVisit.get(nodeId);
			count++;
			this.nodeVisit.put(nodeId, count);
		}else{
			this.nodeVisit.put(nodeId, 1);
		}
	}

	public Map<Integer, Integer> getStateMove() {
		return stateMove;
	}

	public void incStateMove(int stateId){
		if(this.stateMove.containsKey(stateId)){
			int count = this.stateMove.get(stateId);
			count++;
			this.stateMove.put(stateId, count);
		}else{
			this.stateMove.put(stateId, 1);
		}
	}

	public Map<String, Integer> getTokPick() {
		return tokPick;
	}

	public void incNodeTokPick(String nodeId){
		if(this.tokPick.containsKey(nodeId)){
			int count = this.tokPick.get(nodeId);
			count++;
			this.tokPick.put(nodeId, count);
		}else{
			this.tokPick.put(nodeId, 1);
		}
	}
	
	public Map<String, Integer> getTokDrop() {
		return tokDrop;
	}
	
	public void incNodeTokDrop(String nodeId){
		if(this.tokDrop.containsKey(nodeId)){
			int count = this.tokDrop.get(nodeId);
			count++;
			this.tokDrop.put(nodeId, count);
		}else{
			this.tokDrop.put(nodeId, 1);
		}
	}
	
}
