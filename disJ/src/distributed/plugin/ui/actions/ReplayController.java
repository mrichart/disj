package distributed.plugin.ui.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Graph;
import distributed.plugin.ui.editor.GraphEditor;
import distributed.plugin.ui.models.GraphElement;

public class ReplayController implements IController {

	ProcessActions pa;
	GraphEditor editor;
	private Thread holder;
	Runnable proc;
	boolean pause;
	private boolean stepNext;
	private boolean stop;

	public ReplayController(ProcessActions pa, GraphEditor editor) {
		super();
		this.pa = pa;
		this.editor = editor;
		this.stop = true;
	}

	public void executeRun() {

		StringBuffer sb = editor.getRecFile();
		if (stop == true) {
			proc = new PlaybackProcessor(sb);
			pause = false;
			stepNext = false;
			stop = false;
			holder = new Thread(proc, "Playback");
			holder.start();
		} else {
			pause = false;
			stepNext = false;
			stop = false;
		}

	}


	public void executeStepNext() {
		pause = false;
		stepNext = true;

	}


	public void executeStop() {
		if (stop==true){
			this.resetState();
		}else{
		stop = true;
		}

	}


	public void executeSuspend() {

		pause = true;
		stepNext = false;
	}

	class PlaybackProcessor implements Runnable {

		private BufferedReader bsr;

		String line;
		Graph graph = editor.getGraphElement().getGraph();
		Map<String, Node> nodes = graph.getNodes();
		Map<String, Edge> edges = graph.getEdges();

		int state;
		String NodeId;
		String EdgeId;
		Node node;
		Edge edge;
		String[] parts = null;

		public PlaybackProcessor(StringBuffer sb) {
			bsr = new BufferedReader(new StringReader(sb.toString()));
		}

		public void run() {
			try {
				line = bsr.readLine();
				while (line != null) {
					if (!stop && !pause) {

						if (line.startsWith("sc")) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ignore) {
								System.out
										.println("slow down process with speed: "
												+ 500 + " =>" + ignore);
							}
							line = line.substring(3);
							parts = line.split("\\s");

							NodeId = parts[0];
							state = new Integer(parts[2]);
							System.out.println(NodeId + "-->state:" + state);
							node = (Node) nodes.get(NodeId);
							node.firePropertyChange(
									IConstants.PROPERTY_CHANGE_NODE_STATE,
									null, state);
						}

						if (line.startsWith("lvc")) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ignore) {
								System.out
										.println("slow down process with speed: "
												+ 500 + " =>" + ignore);
							}
							line = line.substring(4);
							parts = line.split("\\s");
							boolean visibility = new Boolean(parts[2]);

							EdgeId = parts[0];
							// state = new Integer(parts[2]);
							// System.out.println(NodeId + "-->state:" + state);
// FIXME set link visibility() 							
//							edge = (Edge) edges.get(EdgeId);
//							LinkElement le = edge.getLinkElement();
//							le.setVisible(visibility);
						}

						if (line.startsWith("Node")) {
							NodeId = line.substring(6);
							System.out.println(NodeId);
							node = (Node) nodes.get(NodeId);
							String StateLine = bsr.readLine();
							// System.out.println(StateLine);
							String InitLine = bsr.readLine();
							// System.out.println(InitLine);
							String Init = InitLine.substring(6);
							System.out.println("Init=" + Init);
							// FIXME init should be integer number
							boolean b = new Boolean(Init);
							if(b){
								node.setNumInit(1);
							}else{
								node.setNumInit(0);
							}
							String StarterLine = bsr.readLine();
							// System.out.println(StarterLine);
							String Starter = StarterLine.substring(9);
							System.out.println("Starter=" + Starter);
							node.setAlive(new Boolean(Starter));
							String MsgReceivedLine = bsr.readLine();
							// System.out.println(MsgReceivedLine);
							String MsgReceived = MsgReceivedLine.substring(18);
							System.out.println("MsgReceived=" + MsgReceived);
							node.setNumMsgRecv(new Integer(MsgReceived));
							String MsgSentLine = bsr.readLine();
							// System.out.println(MsgSentLine);
							String MsgSent = MsgSentLine.substring(14);
							System.out.println("MsgSent=" + MsgSent);
							node.setNumMsgSend(new Integer(MsgSent));
						}
						
						if (line.startsWith("Edge")){
							EdgeId = line.substring(6);
							System.out.println();
							System.out.println("EdgeId="+EdgeId);
							edge = (Edge) edges.get(EdgeId);
							String Reliable = bsr.readLine().substring(10);
							System.out.println("Reliable="+Reliable);
							edge.setReliable(new Boolean(Reliable));
							String MsgFlowType = bsr.readLine().substring(19);
							System.out.println("MsgFlowType="+MsgFlowType);
							edge.setMsgFlowType(new Short(MsgFlowType));
							String NumMsg = bsr.readLine().substring(21);
							System.out.println("NumMsg="+NumMsg);
							edge.setTotalMsg(new Integer(NumMsg));
							String DelayType = bsr.readLine().substring(12);
							System.out.println("DelayType="+DelayType);
							edge.setDelayType(new Short(DelayType));
							bsr.readLine();
							bsr.readLine();
						}

						line = bsr.readLine();

						if (stepNext) {
							pause = true;
						}
					}
					if (stop) {
						resetState();
						break;
					}
				}
				if (line == null) {
					stop=true;
					//System.out.println("==Finished==");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	void resetState() {
		/*
		Graph graph = editor.getGraphElement().getGraph();
		Map nodes = graph.getNodes();
		Map edges = graph.getEdges();
		Node node;
		Edge edge;
		for (Object key : nodes.keySet()) {
			node = (Node) nodes.get(key);
			node.setCurState(new Short("99"));
		}
		for (Object key : edges.keySet()){
			edge = (Edge) edges.get(key);
			edge.getLinkElement().setVisible(true);
		}
		*/
		GraphElement graph = editor.getGraphElement();
		graph.resetGraphElement();
	}

}
