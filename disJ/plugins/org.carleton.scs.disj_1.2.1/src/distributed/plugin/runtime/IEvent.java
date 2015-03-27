package distributed.plugin.runtime;

public interface IEvent<T> extends Comparable<T> {

	public int getEventId();
	
	public int getExecTime();
	
	public void setExecTime(int time);
	
	public short getEventType();
	
}
