package distributed.plugin.stat;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public abstract class Statistic implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
		
	public abstract String getName();
	public abstract void reset();
	

	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.removePropertyChangeListener(l);
	}
	
	public void firePropertyChange(String prop, Object old, Object newValue) {
		listeners.firePropertyChange(prop, old, newValue);
	}

	
	
}
