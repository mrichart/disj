package distributed.plugin.runtime;

import java.util.List;

/**
 * Black/White board model API for communication interface of
 * agents, boards and nodes in distributed agent environment
 * 
 * @author rpiyasin
 *
 */
public interface IBoardModel {
	
	/**
	 * Read info that posted on a board of current host
	 * 
	 * @return a current list of record (in a String format) that currently
	 * posted (if exist), otherwise an empty list is returned
	 */
	public List<String> readFromBoard();
	
	/**
	 * Append info to the end of a board of current host
	 * 
	 * Note: A new entry will be appended into the end of the list of records,
	 * if the list is full, the oldest entry will be removed by the system
	 * before a new entry appended
	 * 
	 * @param info a String of data that will be posted on a board
	 */
	public void appendToBoard(String info);
	
	/**
	 * Delete a given record from a board
	 * 
	 * @param info a data record that written on a board and would like
	 * to be removed
	 *  
	 * @return true if the data found and successfully remove otherwise false
	 */
	public boolean removeRecord(String info);

}
