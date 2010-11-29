package distributed.plugin.runtime.engine;

import distributed.plugin.core.Node;
import distributed.plugin.core.Logger.logTag;
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
	
	/**
	 * A token model agent constructor	  
	 * 
	 * @param state A start up state of agent
	 * @throws NullPointerException when user call any API provided
	 * by TokenAgent inside the constructor
	 */
	protected TokenAgent(int state){
		super(state);
		this.curToken = 0;
	}

	public final int countHostToken() {		
		Node node = this.agentOwner.getCurNode();
		return node.getNumToken();
	}

	public final int countMyToken() {
		// This is just to make sure that NullPointerException is thrown
		// when user calls it in constructor
		this.agentOwner.getCurNode();
		
		return this.curToken;
	}

	public final void dropToken() {
		Node node = this.agentOwner.getCurNode();
		if(this.curToken > 0){			
			node.incrementToken(1);
			this.remToken(1);
			
			// update log
			String value = this.agentOwner.getCurNode().getNodeId();
			this.agentOwner.getLogger().logAgent(logTag.AGENT_DROP_TOKEN, 
					this.getAgentId(), value);
			
			// notify
			this.notifyEvent(NotifyType.TOKEN_UPDATE);
			
			// update statistic
			this.agentOwner.getStat().incDrop();
			node.getStat().incNumTokDrop();
		}	
	}

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
		this.addToken(amount);		
		
		// update log
		String[] value = {this.agentOwner.getCurNode().getNodeId(), amount+""};
		this.agentOwner.getLogger().logAgent(logTag.AGENT_PICK_TOKEN, 
				this.getAgentId(), value);
		
		// notify
		this.notifyEvent(NotifyType.TOKEN_UPDATE);
		
		// update statistic
		for(int i =0; i < amount; i++){
			this.agentOwner.getStat().incPick();
			node.getStat().incNumTokPick();
		}
			
	}
	
	/*
	 * Set limit and current number of token at the 
	 * beginning of initiation.
	 */
	final void setMaxToken(int maxNum){
		this.maxToken = maxNum;
		this.curToken = this.maxToken;
	}

	public final int getMaxToken() {
		return this.maxToken;
	}

	final void addToken(int numTok){
		this.curToken += numTok;
	}
	
	final void remToken(int numTok){
		this.curToken -= numTok;
	}
}
