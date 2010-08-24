package distributed.plugin.runtime.engine;

import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.Node;
import distributed.plugin.runtime.IBoardModel;

public abstract class BoardAgent implements IBoardModel {

	private int curState;
	
	private BoardAgentProcessor processor;

	private transient Node orgHost;
	
	private transient Node curNode;

	private MessageConsoleStream systemOut;
	
	
	protected BoardAgent(int state){
		this.curState = state;
		this.processor = null;
		this.curNode = null;
		this.systemOut = null;
	}
	
	/**
	 * Assign processor and the corresponding node to this entity
	 * 
	 * @param processor
	 * @param host
	 */
	void initAgent(BoardAgentProcessor processor, Node host) {
		if (this.processor == null)
			this.processor = processor;

		this.systemOut = this.processor.getConsoleStream();
		
		if (this.orgHost == null){
			this.orgHost = host;
			this.curNode = host;
		}
	}
	
	//public abstract void arrive(String incomingPort);

	@Override
	public List<String> getAllInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get my originated host name when first started
	 * @return
	 */
	public String getMyOrgHost(){
		return this.orgHost.getName();
	}
	
	@Override
	public String getNodeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxSlot() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveTo(String port) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> readFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recordInfo(int index, String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean remove(String info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void writeTo(String info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void alarmRing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void become(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHostState(int state){
		
	}
	
	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAlarm(int time) {
		// TODO Auto-generated method stub

	}

}
