package distributed.plugin.runtime.engine;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IBoardModel;
import distributed.plugin.runtime.IDistributedModel;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcesses;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.editor.GraphEditor;

public class BoardAgentProcessor implements IProcesses {

	private boolean stop;

	private boolean pause;

	private boolean stepForward;

	private int speed;

	private String procName;

	private PrintWriter nodeFile;

	private PrintWriter edgeFile;

	private PrintWriter ConsoleOutputFile;

	private PrintWriter RecFile;

	private Class<IBoardModel> client;

	private Class<IRandom> clientRandom;

	/**
	 * A mapping table of every possible state name and value 
	 * of agent which is defined in user algorithm
	 */	
	private Map<Integer, String> stateFields;

	private EventQueue queue;

	private Graph graph;

	private TimeGenerator timeGen;

	private GraphEditor ge;

	private MessageConsole console;

	private MessageConsoleStream systemOut;

	
	BoardAgentProcessor(GraphEditor ge, Graph graph, Class<IBoardModel> client, Class<IRandom> clientRandom,
			URL out) throws IOException {
		this.ge = ge;
		this.speed = IConstants.SPEED_DEFAULT_RATE;
		this.stop = false;
		this.pause = false;
		
		if (graph == null){
			throw new NullPointerException(IConstants.RUNTIME_ERROR_0);
		}
		if (client == null){
			throw new NullPointerException(IConstants.RUNTIME_ERROR_0);
		}

		this.graph = graph;
		this.procName = graph.getId();
		this.client = client;
		this.clientRandom = clientRandom;
		this.queue = new EventQueue();
		this.timeGen = TimeGenerator.getTimeGenerator();
		this.timeGen.addGraph(graph.getId() + "");
		this.stateFields = new HashMap<Integer, String>();

		this.setSystemOutConsole();
		this.initClientStateVariables();
		
		//this.initLogFile(out);
	}
	
	/**
	 * Get Eclipse plug-in console
	 * 
	 * @param name A unique name of plug-in that uses console
	 * @return
	 */
	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMgr = plugin.getConsoleManager();
		IConsole[] existing = conMgr.getConsoles();
		for (int i = 0; i < existing.length; i++){
			if (name.equals(existing[i].getName())){
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMgr.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	/*
	 * Configure system output to Eclipse Plug-in console
	 */
	private void setSystemOutConsole(){
		this.console = findConsole(IGraphEditorConstants.DISJ_CONSOLE);
		this.systemOut = this.console.newMessageStream();
		System.setOut(new PrintStream(this.systemOut));
		System.setErr(new PrintStream(this.systemOut));
	}
	
	/**
	 * Get a disJ plug-in console
	 * 
	 * @return
	 */
	protected MessageConsoleStream getConsoleStream() {
		return this.systemOut;
	}

	/*
	 * Write exception message to Eclipse plug-in console 
	 * then throw RuntimeException
	 * 
	 * @param e an exception that has been thrown
	 */
	private void throwException(Throwable e){
		this.systemOut.println(e.toString());
		throw new RuntimeException(e);
	}
	
	/*
	 * Get client (agent) states defined by user
	 */
	private void initClientStateVariables() {

		try {
			Field[] states = this.client.getFields();
			Object obj = this.client.newInstance();
			// IRandom ran = this.clientRandom.newInstace();
			for (int i = 0; i < states.length; i++) {
				int mod = states[i].getModifiers();
				if (Modifier.isPublic(mod) && Modifier.isFinal(mod)
						&& states[i].getType().equals(int.class)) {
					String name = states[i].getName();
					String tmpName = name.toLowerCase();
					if ((tmpName.startsWith("state") || tmpName
							.startsWith("_state"))) {
						Integer value = (Integer) states[i].get(obj);
						this.stateFields.put(value, name);
					}
				}
			}
		} catch (Exception e){
			this.throwException(e);
		}
	}

	/*
	 * Load client agent into a host node and create init event(s)
	 */
	private void loadAgent() throws DisJException {

		List<Node> hosts = GraphLoader.getInitNodes(graph);
		try {
			// load agents into host nodes
			for (int i = 0; i < hosts.size(); i++) {
				Node host = hosts.get(i);
				int numAgent = host.getNumInitAgentHost();
				for(int j = 0; j<numAgent; j++){
					BoardAgent agent = GraphLoader.createBoardAgentObject(client);
					agent.initAgent(this, host);
					host.addEntity(agent);
				}
			}		
			// create a set of init events
			int execTime = 0;
			List<Event> events = new ArrayList<Event>();			
			for (int i = 0; i < hosts.size(); i++) {
				Node host = hosts.get(i);
				List<IDistributedModel> list = host.getAllEntities();
				int eventId;
				for(int j = 0; j < list.size(); j++){
					BoardAgent agent = (BoardAgent)list.get(j);
					eventId = timeGen.getLastestId(graph.getId() + "");
					
					// add an event into a queue??
					IMessage msg = new Message("Initialized", new Integer(execTime));
					Event e = new MsgPassingEvent(host.getNodeId(), IConstants.EVENT_INITIATE_TYPE,
							execTime, eventId, host.getNodeId(), msg);
	
					events.add(e);
				}
			}

			// add to the queue
			this.queue.pushEvents(events);

			// update time's lower bound
			this.timeGen.setCurrentTime(graph.getId() + "", this.queue
			.topEvent().getExecTime());
			
		} catch (Exception e) {
			throw new DisJException(IConstants.ERROR_8, e.toString());
		}
	}
	
	
	@Override
	public void internalNotify(String owner, IMessage message)
			throws DisJException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMessage(String sender, List<String> receivers,
			IMessage message) throws DisJException {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		
	}

}
