package distributed.plugin.ui.actions;

import java.util.List;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Graph;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.editor.GraphEditor;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

public class ProtocolExecutionController implements IController {

	ProcessActions procAct;
	GraphEditor editor;

	public ProtocolExecutionController(ProcessActions pa, GraphEditor editor) {
		super();
		this.procAct = pa;
		this.editor = editor;
	}

	public void executeRun() {
		System.out.println("--- executeRun/Resume");

		// first run
		if (procAct.getEngine().isStarted() == false) {
			if (!procAct.validateAction(IGraphEditorConstants.RESUME_ID)){
				return;
			}
			
			procAct.validateSavedGraph(editor);
			GraphElement ge = editor.getGraphElement();
			List<NodeElement> nList = ge.getNodeElements();
			Graph graph = ge.getGraph();
			Node node;
			for (NodeElement n : nList) {
				try {
					node = n.getNode();
					node.resetState(node.getCurState());
					graph.addNode(n.getNodeId(), node);
				} catch (DisJException e) {}
			}
			List <LinkElement> eList = ge.getLinkElements();
			Edge edge;
			for (LinkElement le : eList) {
				try {
					edge = le.getEdge();
					graph.addEdge(le.getEdgeId(), edge);
				} catch (DisJException e) {}
			}
			
			// FIXME a cheated way to refresh the node color
			// to user setting state
//			Map<String, Node> nodes = graph.getNodes();
//			Iterator its = nodes.keySet().iterator();
//			for (Node node = null; its.hasNext();) {
//				node = (Node) nodes.get(its.next());
//				node.resetState(node.getCurState());
//			}
			
			// FIXME why we need this copy???
			editor.getGraphElement().copyGraphElement();
			procAct.getEngine().executeMsgPassing(graph, editor.getClientObject(),
					editor.getClientRandomObject());
		} else {
			try {
				if ((!procAct.getEngine().isRunning()) && procAct.getEngine().isStarted()
						&& procAct.getEngine().isSuspend()) {
					procAct.getEngine().resume();
				} else {
					procAct.missUseActionMsg("Engine is not suspened or stoped.");
				}
			} catch (DisJException e) {
				System.err.println(e);
			}
		}

	}

	
	public void executeStepNext() {
		try {
			if (procAct.getEngine().isSuspend()) {
				procAct.getEngine().stepForward();
			}
		} catch (DisJException e) {
			e.printStackTrace();
		}

	}

	public void executeStop() {
		if (!procAct.validateAction(IGraphEditorConstants.STOP_ID))
			return;

		System.out.println("--- executeStop");
		try {
			if (procAct.getEngine().isStarted())
				procAct.getEngine().terminate();

			if (procAct.getEngine().getOriginGraph() != null) {
				// reset the states and data of a graph
				editor.getGraphElement().resetGraphElement();
			} else {
				procAct.missUseActionMsg("Engine is not running");
			}

		} catch (DisJException e) {
			// e.printStackTrace();
			// System.err.println(e);
		}

	}

	
	public void executeSuspend() {
		if (!procAct.validateAction(IGraphEditorConstants.SUSPEND_ID))
			return;

		System.out.println("--- executeSuspend");
		try {
			// if(!this.getEngine().isRunning())
			// this.missUseActionMsg("Engine is not running");

			if (!procAct.getEngine().isSuspend())
				procAct.getEngine().suspend();
			else {
				procAct.missUseActionMsg("Engine already suspended");
			}
		} catch (DisJException e) {
			e.printStackTrace();
			// System.err.println(e);
		}

	}

}
