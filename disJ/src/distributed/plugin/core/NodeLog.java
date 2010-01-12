/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Me
 * 
 * Keeping track of previouse states of this node
 */
public class NodeLog implements Serializable {

    static final long serialVersionUID = IConstants.SERIALIZE_VERSION;
    
	private String graphId;
	
	private List<String> states;

	private List<String> sentTo; 
	
	private List<String> recvFrom;

	protected NodeLog(String graphId) {
		this.graphId = graphId;
		this.states = new ArrayList<String>();
		this.sentTo = new ArrayList<String>();		
		this.recvFrom = new ArrayList<String>();
	}

	protected void clear(){
		this.states.clear();
		this.sentTo.clear();
		this.recvFrom.clear();
	}

	protected void logState(String state) {
		this.states.add(state);
	}

	protected void logMsgRecv(String log) {
		this.recvFrom.add(log);
	}

	protected void logMsgSent(String log) {
		this.sentTo.add(log);
	}
	
	/**
	 * @see distributed.plugin.core.Logable#getOwnerId()
	 */
	protected String getOwnerId() {
		return this.graphId;
	}
	
	/*
	 * Shrink the log memory by 3/4
	 */
//	private void shrinkMemory(){
//		if(this.sentTo.size() > 20){
//			for(int i =0; i < 15; i++){
//				this.sentTo.remove(i);
//			}
//		}		
//		if(this.recvFrom.size() > 20){
//			for(int i =0; i < 15; i++){
//				this.recvFrom.remove(i);
//			}
//		}
//	}

	/**
	 * @return Returns the recvFrom.
	 */
	protected List<String> getRecvFrom() {
		return recvFrom;
	}
	/**
	 * @return Returns the sentTo.
	 */
	protected List<String> getSentTo() {
		return sentTo;
	}
	/**
	 * @return Returns the states.
	 */
	protected List<String> getStates() {
		return states;
	}
	
	protected void setStates(List<String> list){
	    this.states = list;
	}
}