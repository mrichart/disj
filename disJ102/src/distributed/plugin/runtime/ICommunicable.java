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

import java.io.Serializable;

/**
 * @author npiyasin
 *
 * API for communication between entities in distributed environment
 */
public interface ICommunicable {
	
	/**
	 * Send a ginven message through a given port name. 
	 * If a port name does not exist, there will be no message sending.
	 * 
	 * @param distination An out going port label
	 * @param message A message that need to be sent
	 */
	public void sendTo(String distination, boolean message) ;
	public void sendTo(String distination, short message) ;
	public void sendTo(String distination, int message) ;
	public void sendTo(String distination, float message) ;	
	public void sendTo(String distination, long message) ;
	public void sendTo(String distination, double message) ;
	public void sendTo(String distination, Serializable message) ;
	
	/**
	 *  Send a given message with a given message label 
	 *  through a given port name. 
	 *  If a port name does not exist, there will be no message sending.
	 *  
	 * @param msgLabel A message label
	 * @param distination An out going port label
	 * @param message A message that need to be sent
	 */
	public void sendTo(String msgLabel, String distination, boolean message) ;
	public void sendTo(String msgLabel, String distination, short message) ;
	public void sendTo(String msgLabel, String distination, int message) ;
	public void sendTo(String msgLabel, String distination, float message) ;	
	public void sendTo(String msgLabel, String distination, long message) ;
	public void sendTo(String msgLabel, String distination, double message) ;
	public void sendTo(String msgLabel, String distination, Serializable message) ;
	
	
	/**
	 * Send a given message through a given list of port names.
	 * If a port name does not exist, there will be no message sending.
	 * 
	 * @param distinations A list of out going port labels
	 * @param message A message that need to be sent
	 */
	public void sendTo(String[] distinations, boolean message) ;
	public void sendTo(String[] distinations, short message) ;
	public void sendTo(String[] distinations, int message) ;
	public void sendTo(String[] distinations, float message) ;	
	public void sendTo(String[] distinations, long message) ;
	public void sendTo(String[] distinations, double message) ;
	public void sendTo(String[] distinations, Serializable message) ;
	
	/**
	 * Send a given message with a given message label through 
	 * a given list of port names.
	 * If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel A message label
	 * @param distinations A list of out going port labels
	 * @param message A message that need to be sent
	 */
	public void sendTo(String msgLabel, String[] distinations, boolean message) ;
	public void sendTo(String msgLabel, String[] distinations, short message) ;
	public void sendTo(String msgLabel, String[] distinations, int message) ;
	public void sendTo(String msgLabel, String[] distinations, float message) ;	
	public void sendTo(String msgLabel, String[] distinations, long message) ;
	public void sendTo(String msgLabel, String[] distinations, double message) ;
	public void sendTo(String msgLabel, String[] distinations, Serializable message) ;
	
	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message A message that need to be sent
	 */
	public void sendToAll(boolean message) ;
	public void sendToAll(short message) ;
	public void sendToAll(int message) ;
	public void sendToAll(float message) ;
	public void sendToAll(long message) ;
	public void sendToAll(double message) ;
	public void sendToAll(Serializable message) ;
	
	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel A message label
	 * @param message A message that need to be sent
	 */
	public void sendToAll(String msgLabel, boolean message) ;
	public void sendToAll(String msgLabel, short message) ;
	public void sendToAll(String msgLabel, int message) ;
	public void sendToAll(String msgLabel, float message) ;
	public void sendToAll(String msgLabel, long message) ;
	public void sendToAll(String msgLabel, double message) ;
	public void sendToAll(String msgLabel, Serializable message) ;
	
	
	/**
	 * Send a message to all out going ports execept a port that it
	 * received latest.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as 
	 * out going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message A message that need to be sent
	 */
	public void sendToOthers(boolean message) ;
	public void sendToOthers(short message) ;
	public void sendToOthers(int message) ;
	public void sendToOthers(float message) ;
	public void sendToOthers(long message) ;
	public void sendToOthers(double message) ;
	public void sendToOthers(Serializable message) ;
	
	/**
	 * Send a message with a given message labelto all out going 
	 * ports execept a port that it received latest.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as 
	 * out going port label. Otherwise, all out going ports will be sent.
	 * 
	 *  @param msgLabel A message label
	 * @param message A message that need to be sent
	 */
	public void sendToOthers(String msgLabel, boolean message) ;
	public void sendToOthers(String msgLabel, short message) ;
	public void sendToOthers(String msgLabel, int message) ;
	public void sendToOthers(String msgLabel, float message) ;
	public void sendToOthers(String msgLabel, long message) ;
	public void sendToOthers(String msgLabel, double message) ;
	public void sendToOthers(String msgLabel, Serializable message) ;
		
	/**
	 * Get a current internal state of this entity
	 *
	 */
	public int getState() ;
	
	/**
	 * Set an internal state of an entity. The default value at the beginning is 0
	 * 
	 * @param state
	 */
	public void become(int state) ;
	
	/**
	 * Set an alarm internal clock time interval, The clock will ring after
	 * a given time from the time that is set.
	 * 
	 * @param time An offset of waiting time
	 * 
	 */
	public void setAlarm(int time) ;
	
	/**
	 * Set a new blocking state of a given local port of this entity. By default
	 * each entity's ports are not blocked
	 * 
	 * NOTE: If it is Bidirectional link, it will block both incoming and out going ports
	 * 
	 * @param portLabel A local port label of this enity
	 * @param state True is for block, False is unblock
	 * 
	 */
	public void blockPort(String portLabel, boolean state) ;
	
	/**
	 * Print out a text object into an Eclipse Console, the function will use
	 * toString() methode of that object to print
	 * 
	 * Note: This work only with eclipse plugin
	 * 
	 * @param text An object that need to be printed to a console
	 */
	public void printToConsole(Object text);
	
	

}
