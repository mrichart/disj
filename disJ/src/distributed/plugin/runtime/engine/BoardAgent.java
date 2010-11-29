package distributed.plugin.runtime.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import distributed.plugin.core.Logger.logTag;
import distributed.plugin.runtime.IBoardModel;

/**
 * A mobile agent that uses whiteboard model
 * 
 * @author rpiyasin
 *
 */
public abstract class BoardAgent extends AgentModel implements IBoardModel{
	
	protected BoardAgent(int state){
		super(state);
	}

	public final List<String> readFromBoard(){
		List<String> board = this.agentOwner.getCurNode().getWhiteboard();
		List<String> temp = new ArrayList<String>();
		for (Iterator<String> iterator = board.iterator(); iterator.hasNext();) {
			String msg = iterator.next();
			temp.add(msg);
		}
		
		// update log
		String[] value = {this.agentOwner.getCurNode().getNodeId()};
		this.agentOwner.getLogger().logAgent(logTag.AGENT_READ_TO_BOARD, this.getAgentId(), value);
		
		// update statistic
		this.agentOwner.getStat().incRead();
		
		return temp;
	}
	
	public boolean removeRecord(String info){
		List<String> board = this.agentOwner.getCurNode().getWhiteboard();
		boolean b = board.remove(info);
		
		// update log
		String[] value = {this.agentOwner.getCurNode().getNodeId(), info};
		this.agentOwner.getLogger().logAgent(logTag.AGENT_DELETE_FROM_BOARD, this.getAgentId(), value);
		
		// notify
		this.notifyEvent(NotifyType.BOARD_UPDATE);
		
		// update statistic
		this.agentOwner.getStat().incDelete();
		
		return b; 
	}
	
	public void appendToBoard(String info){
		List<String> board = this.agentOwner.getCurNode().getWhiteboard();
		board.add(info);
		this.agentOwner.getCurNode().setWhiteboard(board);
		
		// update log
		String[] value = {this.agentOwner.getCurNode().getNodeId(), info};
		this.agentOwner.getLogger().logAgent(logTag.AGENT_WRITE_TO_BOARD, this.getAgentId(), value);
		
		// notify
		this.notifyEvent(NotifyType.BOARD_UPDATE);
		
		// update statistic
		this.agentOwner.getStat().incWrite();
	}	
	
}
