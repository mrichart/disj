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
 * A processor that processing requests
 */
public interface IProcessor extends Runnable {
	
	/**
	 * Process a request that comes from a sender node and distributed 
	 * the result to given outgoing ports of the sender
	 * 
	 * @param sender A request node
	 * @param receivers A list of outgoing port of the sender that leads to the receivers
	 * @param message A parameters and data that need to be processed
	 * @throws DisJException If there exist an invalid receiver 
	 */
	public void processReqeust(String sender, List<String> receivers, IMessage message) throws DisJException;
	
	public void setStop(boolean stop);

	public boolean isStop();
	
	public void setPause(boolean pause);
	
	public boolean isPause();
	
	public void setSpeed(int speed);
	
	public int getSpeed();
	
	public void cleanUp();
	
	
	
}
