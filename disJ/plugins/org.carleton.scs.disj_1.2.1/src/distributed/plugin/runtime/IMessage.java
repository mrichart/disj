package distributed.plugin.runtime;

import java.io.Serializable;

import distributed.plugin.core.ITransmissible;

public interface IMessage extends ITransmissible {

	/**
	 * Get a message label of message
	 * 
	 * @return A string label of message
	 */
	public String getLabel();
	
	/**
	 * Get a message content of  message
	 * @return
	 */
	public Serializable getContent();
	
}
