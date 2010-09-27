package distributed.plugin.runtime.engine;

import distributed.plugin.core.Node;
import distributed.plugin.runtime.ITokenModel;

/**
 * A mobile agent that uses Token model
 * 
 * @author rpiyasin
 *
 */
public abstract class TokenAgent extends AgentModel implements ITokenModel {
	
	private int curToken;
	
	private int maxToken;
	
	protected TokenAgent(int state){
		super(state);
		this.curToken = 0;
	}	

	@Override
	public final int countHostToken() {		
		Node node = this.agentOwner.getCurNode();
		return node.getNumToken();
	}

	@Override
	public final int countMyToken() {
		return this.curToken;
	}

	@Override
	public final void dropToken() {
		if(this.curToken > 0){
			Node node = this.agentOwner.getCurNode();
			node.incrementToken(1);
			this.curToken--;
			this.notifyEvent(NotifyType.TOKEN_UPDATE);
		}	
	}

	@Override
	public final void pickupToken(int amount) {
		int temp = this.countHostToken();
		if(amount > temp){
			throw new IllegalArgumentException("@TokenAgent.pickupToken()" 
					+ " amount of picking up token " 
					+ amount + " is more than exist token at the host "
					+ temp);
		}
		if(amount < 0){
			throw new IllegalArgumentException("@TokenAgent.pickupToken()" 
					+ " amount of picking up token " 
					+ amount + " must be more than 0");
		}
		if(amount + this.curToken > this.maxToken){
			throw new IllegalArgumentException("@TokenAgent.pickupToken()" 
					+ " amount of picking up token " 
					+ amount + " will overload number of token that "
				    + " agent allowed to carry " + this.maxToken);
		}
		Node node = this.agentOwner.getCurNode();
		node.decrementToken(amount);
		this.curToken += amount;
		this.notifyEvent(NotifyType.TOKEN_UPDATE);
			
	}
	
	/*
	 * Set limit and current number of token at the 
	 * beginning of initiation.
	 */
	final void setMaxToken(int maxNum){
		this.maxToken = maxNum;
		this.curToken = this.maxToken;
	}

	@Override
	public final int getMaxToken() {
		return this.maxToken;
	}

}
