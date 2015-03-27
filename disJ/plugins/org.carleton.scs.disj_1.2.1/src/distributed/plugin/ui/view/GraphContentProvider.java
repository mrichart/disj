package distributed.plugin.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import distributed.plugin.core.Agent;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.stat.GraphStat;

public class GraphContentProvider implements IStructuredContentProvider,
		PropertyChangeListener {

	private Graph graph;
	private List<Agent> agents; 
	private List<Node> nodes;
	
	private TableViewer agentViewer;
	private TableViewer nodeViewer;
	private Canvas canvas;
	
	public GraphContentProvider(List<Agent> agents, List<Node> nodes){
		this.agents = agents;
		this.nodes = nodes;
		this.graph = null;
	}
	
	public void setGraph(Graph graph){
		this.graph = graph;
	}
	
	public void setAgentViewer(TableViewer agentViewer) {
		this.agentViewer = agentViewer;
	}

	public void setNodeViewer(TableViewer nodeViewer) {
		this.nodeViewer = nodeViewer;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}


	public void dispose() {			
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	
		//System.out.println("inputchange() old: " + oldInput + " new: " + newInput);
	}

	public Object[] getElements(Object inputElement) {
		//System.out.println("getElement() " + inputElement);
		
		if(inputElement instanceof List){
			List list = (List)inputElement;
			return list.toArray();
		}else{
			// inputElement is array of objects from view
			return (Object[])inputElement;
		}
		//return agents.toArray();			
	}

	/*
	 * Start init data from model to gui
	 */
	private void startUp(){
		
		if(this.graph != null){
			Map<String, Agent> amaps = this.graph.getAgents();
			if (this.agents.isEmpty()) {
				Iterator<String> it = amaps.keySet().iterator();
				for (String id = null; it.hasNext();) {
					id = it.next();
					this.agents.add(amaps.get(id));
				}
			}
			
			Map<String, Node> nmaps = this.graph.getNodes();
			if (this.nodes.isEmpty()) {
				Iterator<String> it = nmaps.keySet().iterator();
				for (String id = null; it.hasNext();) {
					id = it.next();
					this.nodes.add(nmaps.get(id));
				}
			}
		}	
	}
	
	/*
	 * Clean up records in GUI
	 */
	private void cleanUp(){
		for(int i =0; i < this.agents.size(); i++){
			this.agentViewer.remove(this.agents.get(i));
		}
		
		for(int i =0; i < this.nodes.size(); i++){
			this.nodeViewer.remove(this.nodes.get(i));
		}
		this.agents.clear();
		this.nodes.clear();

		this.agentViewer.refresh();
		this.nodeViewer.refresh();

	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		Display display = Display.getCurrent();
        Runnable ui = null;
        
        //System.out.println("ContentProvider: PropertyChange: " + prop);
        //this.inputChanged(viewer, evt.getOldValue(), evt.getNewValue());
        
		final Object o = evt.getNewValue();
		
		// ******** AGENT ********
		// ======================
		if (prop.equals(IConstants.PROPERTY_CHANGE_ADD_AGENT)) {
			if(o instanceof Agent){
				this.agents.add((Agent)o);
		        if (display == null) {
					ui = new Runnable() {
						public void run() {
							agentViewer.add(o);
						}
					};
				} 
			}		        
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_REM_AGENT)){			
			if(o instanceof Agent){
				if (display == null) {
					// Do not remove physically, only set state to be death
					ui = new Runnable() {
						public void run() {
							agentViewer.update(o, null);
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATE_AGENT)){
			if(o instanceof Agent){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							agentViewer.update(o, null);
							
							// print states vs agents table
							//GraphContentProvider.printStateVsAgentTable(graph.getAgents());
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_LOC_AGENT)){		
			if(o instanceof Agent){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							agentViewer.update(o, null);						
						}
					};
				} 
			}
		}else if (prop.equals(IConstants.PROPERTY_CHANGE_STATISTIC_AGENT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						agentViewer.refresh();
						canvas.redraw();
					}
				};
			} 
			
			// ******** NODE ********
			// ======================
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_ADD_NODE)){
			if(o instanceof Node){
				this.nodes.add((Node)o);
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);							
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_REM_NODE)){
			if(o instanceof Node){
				// Do not remove physically, only set state to be death
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);							
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATE_NODE)){			
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						nodeViewer.update(o, null);
						
						// print nodes vs states table
						//GraphContentProvider.printStateVsNodeTable(graph.getNodes());	
					}
				};
			} 
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_NUM_TOK_NODE)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);						
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_AGENT_AT_NODE)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);						
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATISTIC_NODE)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						nodeViewer.refresh();
						canvas.redraw();																
					}
				};		
			}
			
			// ******** START ********
			// =======================		
		} else if (prop.equals(IConstants.PROPERTY_START_REPORT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						cleanUp();
						startUp();										
					}
				};
			}
		}
		
			// ******** FINAL ********
			// =======================
	
		/* else if (prop.equals(IConstants.PROPERTY_FINAL_AGENT_TOKEN_REPORT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						//cleanUp();
						
						// print agent states vs token pick
						//GraphContentProvider.printStateVsTokenPick(graph.getAgents());
						
						// print agent states vs token drop
						//GraphContentProvider.printStateVsTokenDrop(graph.getAgents());
					}
				};
			}
		}  else if (prop.equals(IConstants.PROPERTY_FINAL_AGENT_BOARD_REPORT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {	
						//cleanUp();
						// print access type vs count
						//GraphContentProvider.printAccessTypeVsCount(graph.getNodes());						
					}
				};
			}
		} else if (prop.equals(IConstants.PROPERTY_FINAL_MSG_PASSING_REPORT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						//cleanUp();
						// print node vs msg sent table
						//GraphContentProvider.printStateVsMsgSent(graph.getNodes());						
					}
				};
			}
		}
			*/
			
		if (ui != null) {
			display = Display.getDefault();
			display.asyncExec(ui);
		} 
	}
	
	public static void printStateVsAgentTable(Map<String, Agent> agents){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getAgentStateCount(agents);
		
		// get agent
		Iterator<String> itr = agents.keySet().iterator();
		Agent a = null;
		if(itr.hasNext()){
			a = agents.get(itr.next());
			
			// print out the table
			System.out.println("States VS #Agent");
			int count = 0;
			String stateName;
			for (Integer stateId : result.keySet()) {
				count = result.get(stateId);
				stateName = a.getStateName(stateId);
				System.out.println(stateName + " = " + count);
			}
		}
	}
	
	public static void printStateVsNodeTable(Map<String, Node> nodes){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getNodeCurStateCount(nodes);
		
		// get node
		Iterator<String> itr = nodes.keySet().iterator();
		Node a = null;
		if(itr.hasNext()){
			a = nodes.get(itr.next());
			
			// print out the table
			System.out.println("States VS #Node");
			int count = 0;
			String stateName;
			for (Integer stateId : result.keySet()) {
				count = result.get(stateId);
				stateName = a.getStateName(stateId);
				System.out.println(stateName + " = " + count);
			}
		}
	}
	
	public static void printStateVsMsgSent(Map<String, Node> nodes){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getNodeStateMsgSentCount(nodes);
		
		// get node
		Iterator<String> itr = nodes.keySet().iterator();
		Node a = null;
		if(itr.hasNext()){
			a = nodes.get(itr.next());
			
			// print out the table
			System.out.println("States VS #Msg Sent");
			int count = 0;
			String stateName;
			for (Integer stateId : result.keySet()) {
				count = result.get(stateId);
				stateName = a.getStateName(stateId);
				System.out.println(stateName + " = " + count);
			}
		}
	}
	
	public static void printAccessTypeVsCount(Map<String, Node> nodes){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getBoardAccessCount(nodes);
			
		// print out the table
		System.out.println("BoardAccess Type VS #Access");
		int count = 0;
		String accessName;
		for (Integer type : result.keySet()) {
			count = result.get(type);
			if(type == IConstants.BOARD_DEL){
				accessName = "Delete Access";
			}else if (type == IConstants.BOARD_WRITE){
				accessName = "Write Access";
			} else{ 
				accessName = "Read Access";
			}
			System.out.println(accessName + " = " + count);
		}		
	}
	
	public static void printStateVsTokenPick(Map<String, Agent> agents){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getStateTokenPickCount(agents);
		
		// get agent
		Iterator<String> itr = agents.keySet().iterator();
		Agent a = null;
		if(itr.hasNext()){
			a = agents.get(itr.next());
			
			// print out the table
			System.out.println("States VS #Token Picked");
			int count = 0;
			String stateName;
			for (Integer stateId : result.keySet()) {
				count = result.get(stateId);
				stateName = a.getStateName(stateId);
				System.out.println(stateName + " = " + count);
			}
		}
	}
	
	public static void printStateVsTokenDrop(Map<String, Agent> agents){
		
		// get results
		Map<Integer, Integer> result = GraphStat.getStateTokenDropCount(agents);
		
		// get agent
		Iterator<String> itr = agents.keySet().iterator();
		Agent a = null;
		if(itr.hasNext()){
			a = agents.get(itr.next());
			
			// print out the table
			System.out.println("States VS #Token Drop");
			int count = 0;
			String stateName;
			for (Integer stateId : result.keySet()) {
				count = result.get(stateId);
				stateName = a.getStateName(stateId);
				System.out.println(stateName + " = " + count);
			}
		}
	}
}
