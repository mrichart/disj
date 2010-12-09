package distributed.plugin.ui.view;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import distributed.plugin.core.Agent;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;

public class GraphContentProvider implements IStructuredContentProvider,
		PropertyChangeListener {

	private List<Agent> agents; 
	
	private TableViewer agentViewer;
	private TableViewer nodeViewer;
	private Canvas canvas;
	
	public GraphContentProvider(List<Agent> agents){
		this.agents = agents;
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
							agentViewer.add(o);
						}
					};
				} 
			}		        
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_STATE_AGENT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						agentViewer.update(o, null);
					}
				};
			} 
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_LOC_AGENT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						agentViewer.update(o, null);
					}
				};
			} 
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_REM_AGENT)){
			if (display == null) {
				ui = new Runnable() {
					public void run() {
						agentViewer.update(o, null);
					}
				};
			} 
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_AGENT_ARRIVE)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);
						}
					};
				} 
			}
		}  else if (prop.equals(IConstants.PROPERTY_CHANGE_AGENT_LEAVE)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);
						}
					};
				} 
			}
		}  else if (prop.equals(IConstants.PROPERTY_CHANGE_TOKEN_DROP)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_TOKEN_PICK)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o, null);
						}
					};
				} 
			}
		} else if (prop.equals(IConstants.PROPERTY_CHANGE_AGENT_BOARD)){
			if(o instanceof Node){
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							nodeViewer.update(o,null);
						}
					};
				} 
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
