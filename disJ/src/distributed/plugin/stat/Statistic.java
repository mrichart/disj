package distributed.plugin.stat;

import java.io.Serializable;

public abstract class Statistic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public abstract String getName();
	public abstract void reset();
	
}
