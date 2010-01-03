package distributed.plugin.ui.actions;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.engine.Entity;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.editor.GraphEditor;

public class ProtocolExecutionController implements Controller {

	ProcessActions pa;
	GraphEditor editor;

	public ProtocolExecutionController(ProcessActions pa, GraphEditor editor) {
		super();
		this.pa = pa;
		this.editor = editor;
	}

	public void executeRun() {
		System.out.println("--- executeRun/Resume");

		// first run
		if (pa.getEngine().isStarted() == false) {
			if (!pa.validateAction(IGraphEditorConstants.RESUME_ID))
				return;
			pa.validateSavedGraph(editor);
			Graph graph = editor.getGraphElement().getGraph();

			// FIXME a cheated way to refresh the node color
			// to user setting state
			Map nodes = graph.getNodes();
			Iterator its = nodes.keySet().iterator();
			for (Node node = null; its.hasNext();) {
				node = (Node) nodes.get(its.next());
				node.resetState(node.getCurState());
			}
			editor.getGraphElement().copyGraphElement();
			pa.getEngine().execute(graph, editor.getClientObject(),
					editor.getClientRandomObject());
		} else {
			try {
				if ((!pa.getEngine().isRunning()) && pa.getEngine().isStarted()
						&& pa.getEngine().isSuspend()) {
					pa.getEngine().resume();
				} else {
					pa.missUseActionMsg("Engine is not suspened or stoped.");
				}
			} catch (DisJException e) {
				System.err.println(e);
			}
		}

	}

	@Override
	public void executeStepNext() {
		MessageConsoleStream out = Entity.findConsole("DisJ Console")
				.newMessageStream();
		out.println();
		out.println();
		out.println();
		try {
			if (pa.getEngine().isSuspend()) {
				pa.getEngine().stepForward();
			}
		} catch (DisJException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void executeStop() {
		if (!pa.validateAction(IGraphEditorConstants.STOP_ID))
			return;

		System.out.println("--- executeStop");
		try {
			if (pa.getEngine().isStarted())
				pa.getEngine().terminate();

			if (pa.getEngine().getOriginGraph() != null) {
				// reset the states and data of a graph
				editor.getGraphElement().resetGraphElement();
			} else {
				pa.missUseActionMsg("Engine is not running");
			}

		} catch (DisJException e) {
			// e.printStackTrace();
			// System.err.println(e);
		}

	}

	@Override
	public void executeSuspend() {
		if (!pa.validateAction(IGraphEditorConstants.SUSPEND_ID))
			return;

		System.out.println("--- executeSuspend");
		try {
			// if(!this.getEngine().isRunning())
			// this.missUseActionMsg("Engine is not running");

			if (!pa.getEngine().isSuspend())
				pa.getEngine().suspend();
			else {
				pa.missUseActionMsg("Engine already suspended");
			}
		} catch (DisJException e) {
			e.printStackTrace();
			// System.err.println(e);
		}

	}

}
