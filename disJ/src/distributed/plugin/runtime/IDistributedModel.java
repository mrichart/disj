package distributed.plugin.runtime;

public interface IDistributedModel {

	/**
	 * Initialize this entity if it is set as initializer entity
	 * 
	 */
	public abstract void init();

	
	/**
	 * Get a current internal state of this entity
	 *
	 */
	public int getState() ;
	
	/**
	 * Set an internal state of an entity. The default value at the beginning is 0
	 * 
	 * @param state
	 */
	public void become(int state) ;
	
	/**
	 * Method invoke when an internal alarm clock of this entity is ring
	 * 
	 */
	public abstract void alarmRing();

	/**
	 * Set an alarm internal clock time interval, The clock will ring after
	 * a given time from the time that is set.
	 * 
	 * @param time An offset of waiting time (positive integer)
	 * 
	 */
	public void setAlarm(int time) ;
	

}
