package distributed.plugin.runtime.adversary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.runtime.engine.MsgPassingProcessor;

public abstract class MsgPassingControl extends AbstractControl {
		
	/**
	 * Allow adversary to specify the time of initiation of 
	 * a given node ID
	 * 
	 * Note: A returning time MUST be more than a current time, 
	 * otherwise a current time will be returned
	 */
	public int initTimeControl(String nodeId){
		// do nothing here, use default value,
		// up to adversary to implement
		
		return this.getCurrentTime();
	}
	
	
	/**
	 * Allow adversary to control and manipulate incoming message
	 * at a node. This will be called right before it entering
	 * a port of a node
	 * 
	 * @param msg A message that is arriving
	 * @param incomingPort A port label that message is entering through
	 * @param nodeId An ID of a destination node of the message
	 */
	public void arrivalControl(IMessage msg, String incomingPort, String nodeId){
		// do nothing here
		// up to adversary to implement
	}

	/**
	 * Allow adversary to configure an arrival time for message before it  
	 * enters an edge
	 * 
	 * Note: A returning time MUST be more than a current time, 
	 * otherwise a current time + 1 will be returned. 
	 * 
	 * @param msg A message that is entering
	 * @param edgeId An ID of a link that will be used by message
	 * @param nodeId An ID of a node destination of a message
	 * 
	 * @return A simulation time that message will be received by the node
	 */
	public int setArrivalTime(IMessage msg, String edgeId, String nodeId) {
		Node sender;
		int delay = 0;
		int curTime = this.getCurrentTime();
		Edge edge = this.getEdge(edgeId);
		sender = edge.getOthereEnd(this.getNode(nodeId));
		delay = edge.getDelayTime(sender, curTime);
		return delay;
	}

	/**
	 * Allow adversary to configure the lost of message that will
	 * traveling on an edge to a node. This drop will be determined 
	 * right after the message is created
	 * 
	 * @param msg A message
	 * @param edgeId An ID of a link that message is traveling through
	 * @param nodeId An ID of a destination node of the message
	 * @return True if the message will be lost before a node receive,
	 * otherwise false
	 */
	public boolean setDrop(IMessage msg, String edgeId, String nodeId) {
		Random ran = new Random(System.currentTimeMillis());
		Edge edge = this.getEdge(edgeId);
		if(!edge.isReliable()){
			int prob = ran.nextInt(100) + 1;
			return (prob <= edge.getProbOfFailure());
		}else{
			return false;
		}
	}

	/**
	 * Allow adversary to block any message that has a given message label 
	 * at a given port of a node
	 * 
	 * @param msgLable A message label that want to block
	 * @param incomingPort A port that want to block
	 * @param nodeId An ID of a node that wants to block
	 */
	protected final void blockMsg(String msgLable, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		if(this.isInPortExist(nodeId, incomingPort)){
			recv.blockVisitor(msgLable, incomingPort);
		}else{
			throw new IllegalArgumentException("@blockmsg() an in-port "
					+ incomingPort + " does not exist for node ID " + nodeId);
		}
	}
	
	/**
	 * Allow adversary to unblock a port with a given message label at a node.
	 * All messages with the label that has been blocked will be released
	 * 
	 * @param msgLable A message label that want to unblock
	 * @param incomingPort A port that want to unblock
	 * @param nodeId An ID of a node that wants to unblock
	 */
	protected final void unblockMsg(String msgLabel, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		
		if(!this.isInPortExist(nodeId, incomingPort)){
			throw new IllegalArgumentException("@unblockMsg() an in-port "
					+ incomingPort + " does not exist for node ID " + nodeId);
		}
		
		recv.unblockVisitor(msgLabel, incomingPort);
		List<Event> events = recv.getBlockedEvents(incomingPort);
		List<Event> out = new ArrayList<Event>();
		Iterator<Event> its = events.iterator();
		
		// put them in a queue with the current execution time
		int time = this.getCurrentTime();
		for (MsgPassingEvent e = null; its.hasNext(); ){
			e = (MsgPassingEvent)its.next();
			if(e.getEventType() == IConstants.EVENT_ARRIVAL_TYPE){
				if(e.getMessage().getLabel().equals(msgLabel)){
					events.remove(e);
					e.setExecTime(time);
					out.add(e);				
				}
			}
		}
		// pump it to the processor
		this.proc.pushEvents(out);
	}
	

	/**
	 * Check whether a given message label specifically is blocked 
	 * at a given port of a node
	 * 
	 * @param msgLabel A message label
	 * @param incomingPort An incoming port of the node
	 * @param nodeId An ID of a node that is checking
	 * @return True if the port exists and it blocks every incoming message with
	 * a given label, otherwise false
	 */
	protected final boolean isMsgBlocked(String msgLabel, String incomingPort, String nodeId) {
		Node recv = this.getNode(nodeId);
		return recv.isVisitorBlocked(msgLabel, incomingPort);
	}
	
	/**
	 * Get a list of messages(with expected arrival time) currently in 
	 * a link and heading to a given destination node
	 * 
	 * @param edgeId An ID of a link
	 * @param nodeId An ID of a destination node
	 * @return a list of message with expected arrival time if exist, 
	 * otherwise empty list is returned
	 */
	protected final Map<IMessage, Integer> getTravelingMsg(String edgeId, String nodeId){
		Map<IMessage, Integer> tmp = ((MsgPassingProcessor)this.proc).getTravelingMsg(edgeId, nodeId);
		return tmp;
	}
	
	
}
