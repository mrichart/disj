package distributed.plugin.runtime;

public interface IMessage {

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
	public Object getContent();
	
}
