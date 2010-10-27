package distributed.plugin.runtime.adversary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.runtime.engine.MsgPassingProcessor;

public abstract class MsgPassingControl {

	private MsgPassingProcessor proc;
	
	/**
	 * Register a processor to this adversary program
	 * 
	 * @param proc A processor
	 */
	public final void setProcessor(MsgPassingProcessor proc){
		if(proc != null){
			this.proc = proc;
		}
	}
		
	/**
	 * Allow adversary to control and manipulate incoming message
	 * at a node
	 * 
	 * @param msg A message that is arriving
	 * @param incomingPort A port that object is entering through
	 * @param recvNodeId A destination of the message
	 */
	public void arrivalControl(IMessage msg, String incomingPort, String recvNodeId){
		// do nothing here
		// up to adversary to implement
	}

	/**
	 * Allow adversary to configure a delay time for message that is entering 
	 * an edge will arrive at a node
	 * 
	 * Note: A returning time MUST be more than a current time (curTime), 
	 * otherwise a curTime + 1 will be returned
	 * 
	 * @param curTime A current simulation time
	 * @param msg A message that is entering
	 * @param edgeId An edge ID that is currently in used by message
	 * @param nodeId A destination of current message
	 * 
	 * @return A simulation time that the msg will be received by the node
	 */
	public int getDelatyTime(int curTime, IMessage msg, String edgeId, String nodeId) {
		Node sender;
		int delay = 0;
		try {
			Edge edge = this.getEdge(edgeId);
			sender = edge.getOthereEnd(this.getNode(nodeId));
			delay = edge.getDelayTime(sender, curTime);
		} catch (DisJException e) {}
		
		return delay;
	}

	/**
	 * Allow adversary to configure the status of an edge when message
	 * is traveling on the edge 
	 * 
	 * @param edgeId A link of message that traveling through
	 * @param msg A message
	 * @return True if the message will be lost before a node receive,
	 * otherwise false
	 */
	public boolean isLost(String edgeId, IMessage msg) {
		Random ran = new Random(System.currentTimeMillis());
		Edge edge = this.getEdge(edgeId);
		if(!edge.isReliable()){
			int prob = ran.nextInt(100) + 1;
			return (prob > edge.getProbOfFailure());
		}else{
			return false;
		}
	}

	private Node getNode(String nodeId){
		Node n = this.proc.getGraph().getNode(nodeId);
		if(n == null){
			throw new IllegalArgumentException("@getNode() NO node with ID " + nodeId + " found");
		}
		return n;
	}
	
	private Edge getEdge(String edgeId){
		Edge e = this.proc.getGraph().getEdge(edgeId);
		if(e == null){
			throw new IllegalArgumentException("@getEdge() NO edge with ID " + edgeId + " found");
		}
		return e;
	}
	
	/**
	 * Allow adversary to block a given port of a node
	 * 
	 * @param incomingPort
	 * @param nodeId
	 */
	protected final void blockPort(String incomingPort, String nodeId) {
		try {
			Node recv = this.getNode(nodeId);
			recv.setPortBlock(incomingPort, true);
		} catch (DisJException e) {}
	}

	/**
	 * Allow adversary to unblock a given port of a node
	 * 
	 * @param incomingPort
	 * @param nodeId
	 */
	protected final void unblockPort(String incomingPort, String nodeId) {
		try {
			Node recv = this.getNode(nodeId);
			recv.setPortBlock(incomingPort, false);
			
			// flush blocked messages after unblocked port
			List<Event> events = recv.getBlockedEvents(incomingPort);
			if (events != null) {
				// put them in a queue with the current execution time
				int time = this.proc.getCurrentTime();
				for (int i = 0; i < events.size(); i++) {
					MsgPassingEvent e = (MsgPassingEvent) events.get(i);
					e.setExecTime(time);
				}
				this.proc.pushEvents(events);
				recv.removeBlockedPort(incomingPort);
			}			
		} catch (DisJException e) {}
	}
	
	/**
	 * Allow adversary to block a given msg label at a given port of a node
	 * 
	 * @param msgLable A message label that want to block
	 * @param incomingPort A port that want to block
	 * @param nodeId A node that want to block
	 */
	protected final void blockMsg(String msgLable, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		recv.blockVisitor(msgLable, incomingPort);
	}
	
	/**
	 * Allow adversary to unblock a port with a given message label at a node
	 * And all messages with the label that has been blocked will be released
	 * 
	 * @param msgLable A message label that want to unblock
	 * @param incomingPort A port that want to unblock
	 * @param nodeId A node that want to unblock
	 */
	protected final void unblockMsg(String msgLabel, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		recv.unblockVisitor(msgLabel, incomingPort);
		List<Event> events = recv.getBlockedEvents(incomingPort);
		List<Event> out = new ArrayList<Event>();
		Iterator<Event> its = events.iterator();
		for (MsgPassingEvent e = null; its.hasNext(); ){
			e = (MsgPassingEvent)its.next();
			if(e.getMessage().getLabel().equals(msgLabel)){
				out.add(e);
				events.remove(e);
			}		
		}
		// pump it to the processor
		this.proc.pushEvents(out);
	}
	
	/**
	 * Check whether a given incoming port on a given node is blocked to every
	 * incoming message, 
	 * 
	 * @param incomingPort
	 * @param nodeId A node ID
	 * @return True if it blocks every incoming message if,
	 * otherwise false
	 * @throws DisJException If the given incoming port does not exist at a node
	 */
	protected final boolean isPortBlocked(String incomingPort, String nodeId) throws DisJException{
		Node recv = this.getNode(nodeId);
		return recv.isBlocked(incomingPort);				
	}

	/**
	 * Check whether any message with a given message label is blocked 
	 * at a given port of a node
	 * 
	 * @param msgLabel A message label
	 * @param incomingPort An incoming port of the node
	 * @param recvNodeId A node ID
	 * @return True if the port exists and it blocks every incoming message with
	 * a given label, otherwise false
	 */
	protected final boolean isMsgBlocked(String msgLabel, String incomingPort, String recvNodeId) {
		Node recv = this.getNode(recvNodeId);
		return recv.isVisitorBlocked(msgLabel, incomingPort);
	}
	
}
