package distributed.plugin.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.StringTokenizer;

import distributed.plugin.core.Logger.logTag;
import distributed.plugin.runtime.engine.TimeGenerator;

public abstract class LogIO implements ILogable {

	protected String graphId;
	
	protected PrintWriter out;
	
	private URL dirUrl;	
	
	private TimeGenerator timeGen;
	
	public LogIO(String graphId, URL dirUrl, TimeGenerator timeGen){
		if(graphId == null || dirUrl == null || timeGen == null){
			throw new IllegalArgumentException("@Logger.constructor Parameters cannot be null");
		}
		this.graphId = graphId;
		this.dirUrl = dirUrl;
		this.out = null;
		this.timeGen = timeGen;
	}
	
	public void initLog() throws IOException {
		if(out == null){
			File dir = new File(this.dirUrl.getPath());
			StringTokenizer tok = new StringTokenizer(this.graphId, ".");
			String name = tok.nextToken();
			File r = new File(dir, name + ".rec");
			if(r.exists()){
				r.delete();
			}
			this.out = new PrintWriter(new FileWriter(r));
		}
	}

	public void cleanUp() {
		if(this.out != null){
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}

	public int getCurrentTime() {
		return this.timeGen.getCurrentTime(this.graphId);
	}
	
	/**
	 * Log the type of model that is simulating
	 * 
	 * @param tag A tag corresponding to a model
	 * @param className A fully qualified java class name of user entity
	 */
	public void logModel(logTag tag, String className){
		this.out.println(this.getCurrentTime() + "," + tag + ","+ className);
		this.out.flush();
	}

}
