/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.runtime;

import java.util.List;

import distributed.plugin.core.DisJException;


/**
 * @author npiyasin
 * 
 * The processing step that has to be done
 */
public interface IProcesses extends Runnable {
	
	/**
	 * Process a message object
	 * @param sender
	 * @param receivers A list of outgoing edge's name of the sender that leads to the receivers
	 * @param message A message object that need to be processed
	 * @throws DisJException
	 */
	public void processMessage(String sender, List receivers, IMessage message) throws DisJException;
		
	/**
	 * An internal notification
	 * @param owner 
	 * @param message An information provided for this this notification
	 * @throws DisJException
	 */
	public void internalNotify(String owner, IMessage message) throws DisJException;
}
