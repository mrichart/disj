package distributed.plugin.ui.view;

import java.beans.PropertyChangeListener;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.Activator;
import distributed.plugin.ui.models.GraphElement;

public class OverviewAgentPage extends DisJViewPage {

	private GraphElement contents;
	
	private List<Agent> agents; 
	private CTabFolder folder;
	private CTabItem agentTab;
	private CTabItem statTab;
	private CTabItem graphTab;
	
	private TableViewer viewer;
	private Canvas canvas;
	private Group group;
	private org.eclipse.swt.widgets.List list;
	
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	private GraphContentProvider prov;
	private NameSorter sorter;
	private AgentComparator compare;
	
	private static Image IMG_AGENT;
	private static Image IMG_STAT;
	private static ImageDescriptor IDSC_AGENT;
	private static ImageDescriptor IDSC_STAT;
	static{		
		try {
			URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			URL imageUrl = new URL(installUrl, "icons/agent.png");			
			IDSC_AGENT = ImageDescriptor.createFromURL(imageUrl);			
			IMG_AGENT = IDSC_AGENT.createImage();
			
			imageUrl = new URL(installUrl, "icons/stat.png");	
			IDSC_STAT = ImageDescriptor.createFromURL(imageUrl);			
			IMG_STAT = IDSC_STAT.createImage();
			
		} catch (MalformedURLException e) {}		
	}

	class GraphContentProvider implements IStructuredContentProvider, PropertyChangeListener{

		public void dispose() {			
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	
			//System.out.println("inputchange() old: " + oldInput + " new: " + newInput);
		}

		public Object[] getElements(Object inputElement) {
			//System.out.println("getElement() " + inputElement);
			
			// inputElement is array of objects from view
			
			return agents.toArray();			
		}

		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			String prop = evt.getPropertyName();
			Display display = Display.getCurrent();
	        Runnable ui = null;
	        
	        //System.out.println("ContentProvider: PropertyChange: " + prop);
	        
	        //this.inputChanged(viewer, evt.getOldValue(), evt.getNewValue());
			final Object o = evt.getNewValue();
			
			if (prop.equals(IConstants.PROPERTY_CHANGE_ADD_AGENT)) {
				if(o instanceof Agent){
					agents.add((Agent)o);
			        if (display == null) {
						ui = new Runnable() {
							public void run() {
								viewer.add(o);
							}
						};
					} 
				}
		        
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATE_AGENT)){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							viewer.update(o, null);
						}
					};
				} 
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_LOC_AGENT)){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							viewer.update(o, null);
						}
					};
				} 
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_REM_AGENT)){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							viewer.update(o, null);
						}
					};
				} 
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATISTIC_NODE)){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							canvas.redraw();
						}
					};
				} 
			}
			
			if (ui != null) {
				display = Display.getDefault();
				display.asyncExec(ui);
			} 
		}
		
	}
	
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if(obj instanceof Agent){
				Agent a = (Agent)obj;
				if(index == 0){
					return a.getAgentId();
					
				}else if(index == 1){
					if(a.isAlive() == true){
						return "True";
					}else{
						return "False";
					}
				}else if (index == 2) {
					return a.getCurStateName();
					
				} else if (index == 3){
					return a.getCurLocation();
					
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
			return  IMG_AGENT;
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
				rc = (p1.isAlive()+"").compareTo(p2.isAlive()+"");
				break;
			case 2:
				rc = p1.getCurStateName().compareTo(p2.getCurStateName());
				break;
			case 3:
				rc = p1.getCurLocation().compareTo(p2.getCurLocation());
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

	public OverviewAgentPage(GraphElement contents){
		this.agents = new ArrayList<Agent>();
		this.prov = new GraphContentProvider();
		this.compare = new AgentComparator();
		this.setContents(contents);
	}
	
	public void setContents(GraphElement contents) {
		this.contents = contents;
		this.contents.addPropertyChangeListener(this.prov);
	}

	private void loadAgents(){
		Map<String, Agent> maps = this.contents.getGraph().getAgents();
		if(this.agents.isEmpty()){
			Iterator<String> it = maps.keySet().iterator();
			for(String id = null; it.hasNext();){
				id = it.next();
				this.agents.add(maps.get(id));
			}
		}
	}

	public void createControl(Composite parent) {		
		
		// crate multi tab
		this.folder = new CTabFolder(parent, SWT.BORDER);
		this.creatAgentView();
		this.createStatView();
			
		makeActions();
		//hookContextMenu();
		hookDoubleClickAction();
		//contributeToActionBars();
	}
	
	private void createStatView(){
		this.statTab = new CTabItem(this.folder, SWT.NONE);
		this.statTab.setText("Statistic View");
		this.statTab.setImage(IMG_STAT);
		
		// create main composite of the page
		Composite com = new Composite(this.folder, SWT.NONE);
		
		// create canvas
		canvas = new Canvas(com, SWT.NONE);
		canvas.setBackground(com.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	    canvas.setSize(500, 500);  
        canvas.addListener(SWT.Paint, new Listener () {
        	public void handleEvent (Event event) {
        		redrawStatistic(event.gc);		    
			}
        });    
        this.statTab.setControl(com);
		
	}
	
	private void redrawStatistic(GC gc){
		
		// read info from model
		Graph graph = this.contents.getGraph();
		Map<String, Node> nodes = graph.getNodes();
		Map<String, Agent> agents = graph.getAgents();
		GraphStat st = graph.getStat();
		Map<Integer, String> states = graph.getStateFields();
		
		Map<Integer, Integer> ns = st.getNodeCurStateCount(nodes);
		gc.setLineWidth(4);
		int[] tmp = new int[(ns.size()+1)*2];
		tmp[0] = 0;
		tmp[1] = 0;
		Iterator<Integer> it = ns.keySet().iterator();
		int  k = 2;
		for(int j = 0; it.hasNext();k += 2){
			j = ns.get(it.next());			
			tmp[k] = j*10;
			tmp[k+1] = k;
		}
		gc.drawPolyline(tmp);
	    //gc.drawRectangle(10, 10, 40, 45);
	    //gc.drawOval(65, 10, 30, 35);
	    //gc.drawLine(130, 10, 90, 80);
	    gc.drawPolygon(new int[] { 20, 70, 45, 90, 70, 70 });
	    gc.drawPolyline(new int[] { 10, 120, 70, 100, 100, 130, 130, 75 });
	}
	
	private void creatAgentView(){		
		this.agentTab = new CTabItem(this.folder, SWT.NONE);
		this.agentTab.setText("Agent View");
		this.agentTab.setImage(IMG_AGENT);
		
		// set layout
		Composite com = new Composite(this.folder, SWT.NONE);
		FillLayout layout = new FillLayout();
		//layout.type = SWT.HORIZONTAL;
		com.setLayout(layout);

		// add table viewer
		viewer = new TableViewer(com, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION );
		
		
		this.createColumns();
		
		viewer.setContentProvider(prov);
		viewer.setLabelProvider(new ViewLabelProvider());

		// upload agents
		this.loadAgents();
		viewer.setInput(this.agents);

		this.sorter = new NameSorter();
		viewer.setSorter(this.sorter);
			
		// add text display
		group = new Group(com, SWT.SHADOW_ETCHED_IN);
		group.setText("Agent Suitecase");
		FillLayout r = new FillLayout(SWT.VERTICAL);
		group.setLayout(r);

		list = new org.eclipse.swt.widgets.List(group, SWT.BORDER
				| SWT.SCROLL_LINE);
		list.add("info is here");	
		
		this.agentTab.setControl(com);
		
	}

	// This will create the columns for the table
	private void createColumns() {

		String[] titles = {"Agent", "isAlive", "State", "Location"};
		int[] bounds = { 100, 100, 100, 100};
		
		// create each and every column into a TableViewer
		TableViewerColumn column;
		for (int i = 0; i < titles.length; i++) {
			column = new TableViewerColumn(this.viewer, SWT.NONE);
			final TableColumn col = column.getColumn();
			col.setText(titles[i]);
			col.setWidth(bounds[i]);
			col.setResizable(true);
			col.setMoveable(true);			
		
		
			// sorting
			final int index = i;
			col.addSelectionListener(new SelectionAdapter(){				
				@Override
				public void widgetSelected(SelectionEvent e) {
					OverviewAgentPage.this.compare.setColumn(index);
					int dir = viewer.getTable().getSortDirection();
					if (viewer.getTable().getSortColumn() == col) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {
						dir = SWT.DOWN;
					}
					viewer.getTable().setSortDirection(dir);
					viewer.getTable().setSortColumn(col);
					viewer.refresh(); 
				} 
			});	
		}
		Table table = viewer.getTable();
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
    
	private void hookDoubleClickAction() {
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = viewer.getSelection();
				Object o = ((IStructuredSelection)selection).getFirstElement();

				if(o instanceof Agent){
					Agent a = (Agent)o;
					String[] info = a.getInfo();
					list.removeAll();
					for(int i =0; i < info.length; i++){
						if(info[i] != null){
							list.add(info[i]);
						}
					}
					list.redraw();
				}
			}
		});
	}
	
	private void makeActions() {
		action1 = new Action() {
			public void run() {				
//				Control[] c = com.getChildren();
//				for(int i =0; i < c.length; i++){
//					c[i].dispose();
//				}
//				Button b = new Button(com, SWT.PUSH);
//				b.setText("hellooo");
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
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
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
