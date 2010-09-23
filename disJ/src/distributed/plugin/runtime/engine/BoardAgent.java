package distributed.plugin.runtime.engine;

import java.util.List;

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
	
	@Override
	public final List<String> readFromBoard() {
		return this.agentOwner.readFromBoard();
	}

	@Override
	public final boolean removeRecord(String info) {		
		return this.agentOwner.removeFromBoard(info);
	}

	@Override
	public final void appendToBoard(String info) {
		this.agentOwner.appendToBoard(info);
	}
}
