package distributed.plugin.core;

import java.io.IOException;

import distributed.plugin.core.Logger.logTag;

public interface ILogable {

	public void initLog() throws IOException;
	public void cleanUp();
	public int getCurrentTime();
	public void logModel(logTag tag, String className);
}
