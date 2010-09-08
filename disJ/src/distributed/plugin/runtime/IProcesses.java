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
	 * Process a request from a sender and distributed the result to given receives
	 * 
	 * @param sender A request node
	 * @param receivers A list of outgoing port of the sender that leads to the receivers
	 * @param message A parameters and data that need to be processed
	 * @throws DisJException
	 */
	public void processReqeust(String sender, List<String> receivers, IMessage message) throws DisJException;
	
}
