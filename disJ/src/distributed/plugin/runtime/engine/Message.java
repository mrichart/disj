/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.runtime.engine;

import distributed.plugin.runtime.IMessage;

/**
 * @author Me
 *
 * A message wraper
 */
public class Message implements IMessage {

	private String label;
	private Object clientMessage;
	
	protected Message(Object clientMessage){
		this("", clientMessage);
	}
	
	protected Message(String label, Object clientMessage){
		this.label = label;
		this.clientMessage = clientMessage;
	}
	
	/**
	 * Get a content of client message
	 * 
	 * @return Returns clientMessage.
	 */
	public Object getContent() {
		return clientMessage;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
}
