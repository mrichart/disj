package distributed.plugin.runtime;

import distributed.plugin.core.IConstants;

public abstract class Event implements IEvent<Event>{
	
	private short eventType;

	private int eventId;

	private int execTime;
	
	public Event(short eventType, int eventId, int execTime){
		this.eventType = eventType;
		this.eventId = eventId;
		this.execTime = execTime;
	}
	
	public final int getExecTime() {
		return execTime;
	}

	public final void setExecTime(int execTime) {
		this.execTime = execTime;
	}

	public final short getEventType() {
		return eventType;
	}

	public final int getEventId() {
		return eventId;
	}

	public int hashCode() {
		return this.execTime + this.eventType;
	}

	@Override
	public int compareTo(Event e) {
		if (this.execTime < e.getExecTime())
			return -1;
		else if (this.execTime == e.getExecTime()) {
			if (this.eventType < e.getEventType())
				return -1;
			else if (this.eventType == e.getEventType())
				return 0;
			else
				return 1;
		} else
			return 1;
	}
	
	public String toString() {
		return eventId + IConstants.MAIN_DELIMETER +
				+ execTime + IConstants.MAIN_DELIMETER ;
	}

}
