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

import java.io.Serializable;

import distributed.plugin.runtime.IMessage;

/**
 * @author Me
 *
 * A message wrapper
 */
public class Message implements IMessage {

	private static final long serialVersionUID = 1L;
	
	private String label;
	
	private Serializable clientMessage;
	
	protected Message(Serializable clientMessage){
		this("", clientMessage);
	}
	
	protected Message(String label, Serializable clientMessage){
		this.label = label;
		this.clientMessage = clientMessage;
	}
	
	/**
	 * Get a content of client message
	 * 
	 * @return Returns clientMessage.
	 */
	public Serializable getContent() {
		return clientMessage;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
}
