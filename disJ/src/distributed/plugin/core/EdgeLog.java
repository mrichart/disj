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
 * Keeping track of states of this edge
 */
public class EdgeLog implements Serializable {

    static final long serialVersionUID = IConstants.SERIALIZE_VERSION;
	private String graphId;
	private int count; // count number of messages passed through
	private List msgPassed; // a set of message that passed through this link
	// need to be orderded by entered time
	
	public EdgeLog(String graphId){
		this.graphId = graphId;
		this.count = 0;
		this.msgPassed = new ArrayList();
	}

	/**
	 * @return Returns the count.
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * @see distributed.plugin.core.Logable#getOwnerId()
	 */
	public String getOwnerId() {
		return this.graphId;
	}	

	protected void logMsgPassed(String msgLabel, String sender){
		this.count++;
		this.msgPassed.add(msgLabel + IConstants.MAIN_DELIMETER + sender);
	}
	
	/*
	 * Shrink the log memory by 3/4
	 */
	private void shrinkMemory(){
		if(this.msgPassed.size() > 20){
			for(int i =0; i < 15; i++){
				this.msgPassed.remove(i);
			}
		}		
	}
	
}
