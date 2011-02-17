package distributed.plugin.ui.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

import distributed.plugin.core.Agent;
import distributed.plugin.core.Graph;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.engine.AgentModel;
import distributed.plugin.runtime.engine.TokenAgent;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.Activator;
import distributed.plugin.ui.models.GraphElement;

public class OverviewDisjPage extends DisJViewPage {

	private static final int LINE_THICK = 4;
	private static final int BAR_LENGTH = 140;
	private static final int BAR_HIGHT = 140;
	private static final int BAR_WIDTH = 20;
	private static final int BAR_GAP = 4;
	private static final int NUM_BAR = 6;
	private static final int TEXT_HIGH = 20;
	
	// coordinate of starting point of each section
	private static final List<int[]> CORDINATE;
	static{
		CORDINATE = new ArrayList<int[]>();
		// section ..
		CORDINATE.add(new int[]{820,20});
		
		CORDINATE.add(new int[]{20,20});
		CORDINATE.add(new int[]{420,20});
		
		CORDINATE.add(new int[]{20,220});
		CORDINATE.add(new int[]{420,220});
		
		CORDINATE.add(new int[]{20,420});
		CORDINATE.add(new int[]{420,420});
	}
	
	private GraphElement contents;

	private List<Agent> agents;
	private List<Node> nodes;

	private CTabFolder folder;
	private CTabItem agentTab;
	private CTabItem nodeTab;
	private CTabItem statTab;
	private Group agentGroup;
	private Group nodeGroup;
	private org.eclipse.swt.widgets.List suitecase;
	private org.eclipse.swt.widgets.List board;

	private TableViewer agentViewer;
	private TableViewer nodeViewer;
	private Canvas canvas;

	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	private GraphContentProvider prov;

	private NameSorter sorter;
	private AgentComparator agentCompare;
	private NodeComparator nodeCompare;

	private static Image IMG_AGENT;
	private static Image IMG_STAT;
	private static Image IMG_NODE;
	private static ImageDescriptor IDSC_AGENT;
	private static ImageDescriptor IDSC_STAT;
	private static ImageDescriptor IDSC_NODE;
	static {
		try {
			URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			URL imageUrl = new URL(installUrl, "icons/agent.png");
			IDSC_AGENT = ImageDescriptor.createFromURL(imageUrl);
			IMG_AGENT = IDSC_AGENT.createImage();

			imageUrl = new URL(installUrl, "icons/stat.png");
			IDSC_STAT = ImageDescriptor.createFromURL(imageUrl);
			IMG_STAT = IDSC_STAT.createImage();

			imageUrl = new URL(installUrl, "icons/token.png");
			IDSC_NODE = ImageDescriptor.createFromURL(imageUrl);
			IMG_NODE = IDSC_NODE.createImage();

		} catch (MalformedURLException e) {
		}
	}

	class AgentViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (obj instanceof Agent) {
				Agent a = (Agent) obj;
				if (index == 0) {
					return a.getAgentId();

				} else if (index == 1) {
					if (a.isAlive() == true) {
						return "True";
					} else {
						return "False";
					}
				} else if (index == 2) {
					return a.getCurStateName();

				} else if (index == 3) {
					return a.getCurLocation();

				} else if (index == 4) {
					AgentModel a1 = a.getClientEntity();
					if(a1 instanceof TokenAgent){
						TokenAgent t1 = (TokenAgent)a1;
						return(t1.countMyToken()+"");
					}
					return 0+"";
					
				} else if (index == 5) {
					return a.getPastStates().toString();
				} 
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {

			if (index == 0) {
				return getImage(obj);
			}
			return null;

		}

		public Image getImage(Object obj) {
			return IMG_AGENT;
		}
	}

	class NodeViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (obj instanceof Node) {
				Node a = (Node) obj;
				if (index == 0) {
					return a.getNodeId();

				} else if (index == 1) {
					if (a.isAlive() == true) {
						return "True";
					} else {
						return "False";
					}
				} else if (index == 2) {
					return a.getCurStateName();

				} else if (index == 3) {
					return a.getNumToken()+"";

				} else if (index == 4) {
					return a.getAllAgents().size()+"";

				}
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {

			if (index == 0) {
				return getImage(obj);
			}
			return null;

		}

		public Image getImage(Object obj) {
			return IMG_NODE;
		}
	}

	class NameSorter extends ViewerSorter {
	}

	class AgentComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public AgentComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Agent p1 = (Agent) e1;
			Agent p2 = (Agent) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = p1.getAgentId().compareTo(p2.getAgentId());
				break;
			case 1:
				rc = (p1.isAlive() + "").compareTo(p2.isAlive() + "");
				break;
			case 2:
				rc = p1.getCurStateName().compareTo(p2.getCurStateName());
				break;
			case 3:
				rc = p1.getCurLocation().compareTo(p2.getCurLocation());
			case 4:
				AgentModel a = p1.getClientEntity();
				AgentModel b = p2.getClientEntity();
				if(a instanceof TokenAgent){
					TokenAgent t1 = (TokenAgent)a;
					TokenAgent t2 = (TokenAgent)b;
					rc = (t1.countMyToken()+"").compareTo(t2.countMyToken()+"");
				}else{
					rc = 0;
				}				
				break;
			case 5:
				// No comparison for a list of state string
				rc = 0;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}

	class NodeComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public NodeComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Node p1 = (Node) e1;
			Node p2 = (Node) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = p1.getNodeId().compareTo(p2.getNodeId());
				break;
			case 1:
				rc = (p1.isAlive() + "").compareTo(p2.isAlive() + "");
				break;
			case 2:
				rc = p1.getCurStateName().compareTo(p2.getCurStateName());
				break;
			case 3:
				rc = (p1.getNumToken() + "").compareTo(p2.getNumToken() + "");
				break;
			case 4:
				rc = (p1.getAllAgents().size() + "").compareTo(p2
						.getAllAgents().size()
						+ "");
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}

	public OverviewDisjPage(GraphElement contents) {
		this.agents = new ArrayList<Agent>();
		this.nodes = new ArrayList<Node>();
		this.prov = new GraphContentProvider(this.agents, nodes);
		this.agentCompare = new AgentComparator();
		this.nodeCompare = new NodeComparator();
		this.setContents(contents);
	}

	public void setContents(GraphElement contents) {
		if(contents != null){
			this.contents = contents;
			this.contents.addPropertyChangeListener(this.prov);
			this.prov.setGraph(this.contents.getGraph());
		}
	}

//	void loadAgents() {
//		Map<String, Agent> maps = this.contents.getGraph().getAgents();
//		if (this.agents.isEmpty()) {
//			Iterator<String> it = maps.keySet().iterator();
//			for (String id = null; it.hasNext();) {
//				id = it.next();
//				this.agents.add(maps.get(id));
//			}
//		}
//	}
//
	void loadNodes() {
		Map<String, Node> maps = this.contents.getGraph().getNodes();
		if (this.nodes.isEmpty()) {
			Iterator<String> it = maps.keySet().iterator();
			for (String id = null; it.hasNext();) {
				id = it.next();
				this.nodes.add(maps.get(id));
			}
		}
	}

	public void createControl(Composite parent) {

		// crate multi tab
		this.folder = new CTabFolder(parent, SWT.BORDER);
		this.creatAgentView();
		this.creatNodeView();
		this.createStatisticView();

		this.clickSelectAction();
		
		makeActions();
		
		// hookContextMenu();		
		// contributeToActionBars();
	}

	private void createStatisticView() {
		this.statTab = new CTabItem(this.folder, SWT.NONE);
		this.statTab.setText("Statistic View");
		this.statTab.setImage(IMG_STAT);

		// create main composite of the page
		Composite com = new Composite(this.folder, SWT.NONE);

		// create canvas
		canvas = new Canvas(com, SWT.NONE);
		canvas.setBackground(com.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		canvas.setSize(1400, 1000);
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				redrawStatistic(event.gc);
			}
		});
		this.prov.setCanvas(canvas);
		this.statTab.setControl(com);
	}

	private void redrawStatistic(GC gc) {
		int i;
		int curX;
		int curY;
		int max;
		int totNode;
		int totAgent;
		
		// read info from model
		Graph graph = this.contents.getGraph();
		Map<String, Node> nodes = graph.getNodes();
		Map<String, Agent> agents = graph.getAgents();
		GraphStat st = graph.getStat();
		Map<Integer, String> states = graph.getStateFields();

		// get default color
		Color deFor = gc.getForeground();
		Color deBac = gc.getBackground();
		
		// define basic draw X and Y axis		
		gc.setLineWidth(LINE_THICK);
		
		int totState = this.contents.getNumStateColor();
		float r = totState/NUM_BAR;
		int xLength = BAR_LENGTH;
		if(r > 1){
			xLength = (int)(BAR_LENGTH * r);
		}		
		
		// section 0: draw state's colors legend
		// =====================================
		Map<Integer, String> smap = contents.getGraph().getStateFields();
			
		// top left of chart
		int x = CORDINATE.get(0)[0];
		int y = CORDINATE.get(0)[1];
		
		// draw squares of color a long side with state name
		// from top to bottom
		Color c;
		for(int state : smap.keySet()){
			// set to default color
			gc.setBackground(deBac);
			
			// get state color
			c = contents.getColor(state);			
			
			// draw a text on the right of color box
			gc.drawText(smap.get(state), x + 40, y);
			
			// draw a box
			gc.drawRectangle(x, y, 20, 20);
			gc.setBackground(c);
			gc.fillRectangle(x, y, 20, 20);
						
			y = y + 40;
		}
		
		// clear up the color
		gc.setForeground(deFor);
		gc.setBackground(deBac);
		
		// section 1: draw a bar chart of nod's state vs #node
		// ===================================================
		totNode = nodes.size();
		Map<Integer, Integer> ns = st.getNodeCurStateCount(nodes);						
		
		// top left of chart
		x = CORDINATE.get(1)[0];
		y = CORDINATE.get(1)[1];
		int[] bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Nodes", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 0;
		max = totNode;
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}		
			
		// section 2: draw a bar chart of node's state vs #msg sent
		// ========================================================
		totNode = nodes.size();
		ns = st.getNodeStateMsgSentCount(nodes);
				
		// top left of chart
		x = CORDINATE.get(2)[0];
		y = CORDINATE.get(2)[1];
		bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Msg Sent", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 1;
		max = st.getTotalMsgSent(nodes);
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}
				
		// section 3: draw a bar chart of agent's state vs #agent
		// ======================================================
		totAgent = agents.size();
		ns = st.getAgentStateCount(agents);
				
		// top left of chart
		x = CORDINATE.get(3)[0];
		y = CORDINATE.get(3)[1];
		bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Agents", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 1;
		max = totAgent;
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}
	
		// section 4: draw a bar chart of agent's state vs #move
		// =====================================================
		totAgent = agents.size();
		ns = st.getStateMoveCount(agents);
				
		// top left of chart
		x = CORDINATE.get(4)[0];
		y = CORDINATE.get(4)[1];
		bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Move", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 1;
		max = st.getTotalAgentMove(agents);
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}
		
		
		// section 5: draw a bar chart of agent's state vs #drop token
		// ===========================================================
		totAgent = agents.size();
		ns = st.getStateTokenDropCount(agents);
				
		// top left of chart
		x = CORDINATE.get(5)[0];
		y = CORDINATE.get(5)[1];
		bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Token Drop", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 1;
		max = st.getTotalTokDrop(agents);
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}	
		
		// section 6: draw a bar chart of agent's state vs #pickup token
		// =============================================================
		totAgent = agents.size();
		ns = st.getStateTokenPickCount(agents);
				
		// top left of chart
		x = CORDINATE.get(6)[0];
		y = CORDINATE.get(6)[1];
		bar1 = new int[]{x, y, x, y+BAR_HIGHT, x+xLength, y+BAR_HIGHT};
		gc.drawText("# Token Pick", x-TEXT_HIGH, y-TEXT_HIGH);
		gc.drawPolyline(bar1);		
		gc.drawText("States", x+(xLength/2), (y+BAR_HIGHT+BAR_GAP));		
		
		// compute number and size of bar 
		i = 1;
		max = st.getTotalTokPick(agents);
		curX = x + BAR_GAP;
		for (int state : ns.keySet()) {
			int count = ns.get(state);
			int per = (100*count)/max;
			Color color = this.contents.getColor(state);
			gc.setForeground(color);
			gc.setBackground(color);
			
			curY = y +(BAR_HIGHT-per-LINE_THICK);
			gc.drawRectangle(curX, curY, BAR_WIDTH, per);
			gc.fillRectangle(curX, curY, BAR_WIDTH, per);
			
			gc.setForeground(deFor);
			gc.setBackground(deBac);
			gc.drawText(count+"", curX, curY-TEXT_HIGH);
			
			i++;
			curX = curX + BAR_WIDTH + BAR_GAP;
		}	
		
	}


	
	private void creatAgentView() {
		this.agentTab = new CTabItem(this.folder, SWT.NONE);
		this.agentTab.setText("Agent View");
		this.agentTab.setImage(IMG_AGENT);

		// set layout
		Composite com = new Composite(this.folder, SWT.NONE);
		FillLayout layout = new FillLayout();
		// layout.type = SWT.HORIZONTAL;
		com.setLayout(layout);

		// add table viewer
		agentViewer = new TableViewer(com, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		this.createAgentColumns();
		this.prov.setAgentViewer(this.agentViewer);

		agentViewer.setContentProvider(this.prov);
		agentViewer.setLabelProvider(new AgentViewLabelProvider());

		// upload agents
		//this.loadAgents();
		agentViewer.setInput(this.agents);

		this.sorter = new NameSorter();
		agentViewer.setSorter(this.sorter);

		// add text display
		agentGroup = new Group(com, SWT.SHADOW_ETCHED_IN);
		agentGroup.setText("Agent Suitecase");
		FillLayout r = new FillLayout(SWT.VERTICAL);
		agentGroup.setLayout(r);

		suitecase = new org.eclipse.swt.widgets.List(agentGroup, SWT.BORDER
				| SWT.SCROLL_LINE);
		suitecase.add("info is here");

		this.agentTab.setControl(com);

	}

	private void creatNodeView() {
		this.nodeTab = new CTabItem(this.folder, SWT.NONE);
		this.nodeTab.setText("Node View");
		this.nodeTab.setImage(IMG_NODE);

		// set layout
		Composite com = new Composite(this.folder, SWT.NONE);
		FillLayout layout = new FillLayout();
		com.setLayout(layout);

		// add table viewer
		nodeViewer = new TableViewer(com, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		this.createNodeColumns();
		this.prov.setNodeViewer(this.nodeViewer);

		nodeViewer.setContentProvider(this.prov);
		nodeViewer.setLabelProvider(new NodeViewLabelProvider());

		// upload every nodes
		this.loadNodes();
		nodeViewer.setInput(this.nodes);

		// this.sorter = new NameSorter();
		// viewer.setSorter(this.sorter);

		// add text display
		nodeGroup = new Group(com, SWT.SHADOW_ETCHED_IN);
		nodeGroup.setText("Whiteboard");
		FillLayout r = new FillLayout(SWT.VERTICAL);
		nodeGroup.setLayout(r);

		board = new org.eclipse.swt.widgets.List(nodeGroup, SWT.BORDER
				| SWT.SCROLL_LINE);
		board.add("board info is here");

		this.nodeTab.setControl(com);

	}

	// This will create the columns for the table
	private void createAgentColumns() {

		String[] titles = { "Agent", "isAlive", "Current State", "Location", "#Token", "State List" };
		int[] bounds = {100, 100, 100, 100, 100, 100};

		// create each and every column into a TableViewer
		TableViewerColumn column;
		for (int i = 0; i < titles.length; i++) {
			column = new TableViewerColumn(this.agentViewer, SWT.NONE);
			final TableColumn col = column.getColumn();
			col.setText(titles[i]);
			col.setWidth(bounds[i]);
			col.setResizable(true);
			col.setMoveable(true);

			// sorting
			final int index = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					OverviewDisjPage.this.agentCompare.setColumn(index);
					int dir = agentViewer.getTable().getSortDirection();
					if (agentViewer.getTable().getSortColumn() == col) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {
						dir = SWT.DOWN;
					}
					agentViewer.getTable().setSortDirection(dir);
					agentViewer.getTable().setSortColumn(col);
					agentViewer.refresh();
				}
			});
		}
		Table table = agentViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setEnabled(true);
	}

	// This will create the columns for the table
	private void createNodeColumns() {

		String[] titles = { "Node ID", "isAlive", "Node State", "# Token",
				"# Agent" };
		int[] bounds = { 100, 100, 100, 100, 100 };

		// create each and every column into a TableViewer
		TableViewerColumn column;
		for (int i = 0; i < titles.length; i++) {
			column = new TableViewerColumn(this.nodeViewer, SWT.NONE);
			final TableColumn col = column.getColumn();
			col.setText(titles[i]);
			col.setWidth(bounds[i]);
			col.setResizable(true);
			col.setMoveable(true);

			// sorting
			final int index = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					OverviewDisjPage.this.nodeCompare.setColumn(index);
					int dir = nodeViewer.getTable().getSortDirection();
					if (nodeViewer.getTable().getSortColumn() == col) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {
						dir = SWT.DOWN;
					}
					nodeViewer.getTable().setSortDirection(dir);
					nodeViewer.getTable().setSortColumn(col);
					nodeViewer.refresh();
				}
			});
		}
		Table table = nodeViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setEnabled(true);
	}

	/**
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	public Control getControl() {
		return this.folder;
	}

	/**
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		this.contents.removePropertyChangeListener(this.prov);
	}

	/**
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	public void setFocus() {
		if (getControl() != null)
			getControl().setFocus();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// not support
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		// not support
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		// not support
	}

	private void clickSelectAction() {
		agentViewer
				.addPostSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						ISelection selection = agentViewer.getSelection();
						Object o = ((IStructuredSelection) selection)
								.getFirstElement();

						if (o instanceof Agent) {
							Agent a = (Agent) o;
							String[] info = a.getInfo();
							suitecase.removeAll();
							for (int i = 0; i < info.length; i++) {
								if (info[i] != null) {
									suitecase.add(info[i]);
								}
							}
							suitecase.redraw();
						}
					}
				});
		
		nodeViewer
		.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = nodeViewer.getSelection();
				Object o = ((IStructuredSelection) selection)
						.getFirstElement();

				if (o instanceof Node) {
					Node a = (Node) o;
					List<String> info = a.getWhiteboard();
					board.removeAll();
					for (int i = 0; i < info.size(); i++) {
						board.add(info.get(i));
					}
					board.redraw();
				}
			}
		});
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				// Control[] c = com.getChildren();
				// for(int i =0; i < c.length; i++){
				// c[i].dispose();
				// }
				// Button b = new Button(com, SWT.PUSH);
				// b.setText("hellooo");
			}
		};
		action1.setText("Agent View");
		action1.setToolTipText("Agent View");
		action1.setImageDescriptor(IDSC_AGENT);

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = agentViewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(agentViewer.getControl().getShell(),
				"Sample View", message);
	}

	private void contributeToActionBars() {
		IActionBars bars = this.getSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

}
