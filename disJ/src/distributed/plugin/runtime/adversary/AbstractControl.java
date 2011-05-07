package distributed.plugin.runtime.adversary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.Edge;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.IProcessor;
import distributed.plugin.runtime.MsgPassingEvent;

public abstract class AbstractControl {
	
	IProcessor proc;	
	
	/*
	 * Register a processor to this adversary program
	 * @param proc A processor
	 */
	public final void setProcessor(IProcessor proc){
		if(proc != null){
			this.proc = proc;
		}
	}
	
	Node getNode(String nodeId){
		Node n = this.proc.getGraph().getNode(nodeId);
		if(n == null){
			throw new IllegalArgumentException("@getNode() NO node with ID " 
					+ nodeId + " found");
		}
		return n;
	}
	
	Edge getEdge(String edgeId){
		Edge e = this.proc.getGraph().getEdge(edgeId);
		if(e == null){
			throw new IllegalArgumentException("@getEdge() NO edge with ID " 
					+ edgeId + " found");
		}
		return e;
	}
	
	boolean isInPortExist(String nodeId, String inPort){
		Node n = this.getNode(nodeId);
		List<String> ports = n.getIncomingPorts();
		return ports.contains(inPort);
	}
/*	
	boolean isOutPortExist(String nodeId, String outPort){
		Node n = this.getNode(nodeId);
		List<String> ports = n.getOutgoingPorts();
		return ports.contains(outPort);
	}
*/		
	
	/**
	 * Get a current simulation time
	 * 
	 * @return
	 */
	protected final int getCurrentTime(){
		return this.proc.getCurrentTime();
	}
	
	/**
	 * Allow adversary to block a given port of a node
	 * 
	 * @param incomingPort A port label that will be unblocked
	 * @param nodeId An ID of a node that want to block
	 */
	protected final void blockPort(String incomingPort, String nodeId) {
		Node recv = this.getNode(nodeId);
		recv.setBlockPort(incomingPort, true);
	}

	/**
	 * Allow adversary to unblock a given port of a node
	 * 
	 * @param incomingPort A port label that will be unblocked
	 * @param nodeId An ID of a node that wants to unblock
	 */
	protected final void unblockPort(String incomingPort, String nodeId) {
		Node recv = this.getNode(nodeId);
		recv.setBlockPort(incomingPort, false);
		
		// flush blocked messages after unblocked port
		List<Event> events = recv.getBlockedEvents(incomingPort);
		if (events != null) {
			// put them in a queue with the current execution time
			int time = this.getCurrentTime();
			for (int i = 0; i < events.size(); i++) {
				MsgPassingEvent e = (MsgPassingEvent) events.get(i);
				e.setExecTime(time);
			}
			// clear the port
			recv.removeBlockedEvents(incomingPort);
			
			// pump it back to processing queue
			this.proc.pushEvents(events);
			
		}
	}
	
	/**
	 * Check whether a given incoming port on a given node is blocked 
	 * 
	 * @param incomingPort
	 * @param nodeId An ID of a node that is checking
	 * @return True if it blocks every incoming message if,
	 * otherwise false
	 */
	protected final boolean isPortBlocked(String incomingPort, String nodeId) {
		Node recv = this.getNode(nodeId);
		return recv.isBlocked(incomingPort);			
	}

	/**
	 * Get a list of all node ID existing in a topology
	 * 
	 * @return
	 */
	protected final List<String> getAllNodeId(){
		Map<String, Node> map = this.proc.getGraph().getNodes();
		List<String> ids = new ArrayList<String>();
		ids.addAll(map.keySet());
		return ids;
	}
	
	/**
	 * Get a name of a given node ID
	 * 
	 * Note: By default node name and node ID are the same.
	 * Node ID created and used by the simulation and cannot 
	 * be modified, while node name is for display and modifiable
	 * by user.
	 * 
	 * @param nodeId
	 * @return
	 */
	protected final String getNodeName(String nodeId){
		Node n = this.proc.getGraph().getNode(nodeId);
		return n.getName();
	}
	
	/**
	 * Get current state of a given node Id
	 * 
	 * @param nodeId
	 * @return A state as an integer if node ID exist otherwise
	 * exception is thrown
	 */
	protected final int getNodeState(String nodeId){
		Node n = this.proc.getGraph().getNode(nodeId);
		if(n == null){
			throw new IllegalArgumentException("@getNodeState() A nodeId " 
					+ nodeId + " does not exist");
		}
		return n.getCurState();
	}
	
	
}
