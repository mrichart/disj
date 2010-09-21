/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.runtime.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.IProcessor;

/**
 * @author npiyasin
 * 
 * Simulation loader and starter, a bridge between UI and core engine
 * It is one-to-one mapping to a graph in GraphEditor and IProcessor
 */
public class SimulatorEngine {
    
    private boolean started;
    
	private URL outLocation;
  
    private IProcessor proc;

    private String graphId;

    private Thread holder;

    public SimulatorEngine() {
        this.started = false;
        this.proc = null;
        this.graphId = null;
        this.holder = null;
    }

    /**
	 * Get Eclipse plug-in console
	 * 
	 * @param name A unique name of plug-in that uses console
	 * @return
	 */
	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMgr = plugin.getConsoleManager();
		IConsole[] existing = conMgr.getConsoles();
		for (int i = 0; i < existing.length; i++){
			if (name.equals(existing[i].getName())){
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMgr.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	
    /**
     * Create Message Passing Model processor with a given graphId and client Class
     * 
     * @param graphId
     * @param client
     * @param clienRandom
     */
    public void execute(Graph graph, Class client, Class<IRandom> clientRandom) {
        try {

           this.graphId = graph.getId();
           if(Entity.class.isAssignableFrom(client)){
        	   Class<Entity> c = (Class<Entity>) client;
        	   this.proc = new MsgPassingProcessor(graph, c, clientRandom, this.outLocation);
        	   
           }else if(BoardAgent.class.isAssignableFrom(client)){
        	   Class<BoardAgent> c = (Class<BoardAgent>) client;
        	   this.proc = new BoardAgentProcessor(graph, c, clientRandom, this.outLocation);
        	   
           }else {
        	   System.out.println("No support for class " + client);
        	   return;
           }

            // FIXME risk of non atomic for next 2 lines
            this.started = true;
            this.holder = new Thread(proc, "DisJ Simulator Engine: " + this.graphId);
            this.holder.start();
            System.out.println("*****[SimulatorEngine] DisJ processor is started*****");

           
        } catch (Exception e) {
        	System.err.println("ERROR @SimultorEngine.execute() " + e);
        }
    }

    /**
     * Get a graphId that is currently under process of execution by 
     * this engine
     * 
     * @return ID of a graph, NULL if there is no graph has not been 
     * assigned for execution to this engine
     */
    public String getCurGraphId(){
    	return this.graphId;
    }
    
    public boolean isStarted() {
        return this.started;
    }

    public void terminate() throws DisJException {
        
        this.started = false;
        
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);

        if (this.proc != null)
            this.proc.setStop(true);
        
        if(this.holder == null)
            return;
        
        if(this.holder.isAlive()){          
            try {
                this.holder.interrupt();
            } catch (SecurityException ignore) {
            }
        }
        this.holder = null;      
    }
    
    public void stepForward() throws DisJException {
        
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);

        if (this.proc == null)
            throw new DisJException(IConstants.ERROR_19);
        
        this.proc.setPause(false);
        //this.proc.setStepForward(true);
    }
    

    public void suspend() throws DisJException {        
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);

        if (this.proc == null)
            throw new DisJException(IConstants.ERROR_19);
        
        proc.setPause(true);
    }

    public boolean isSuspend() throws DisJException {
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);
      
        if (this.proc == null)
            throw new DisJException(IConstants.ERROR_19);

        return proc.isPause();
    }

    public void resume() throws DisJException {
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);

        if (this.proc == null)
            throw new DisJException(IConstants.ERROR_19);

        this.proc.setPause(false);
        //this.proc.setStepForward(false);
    }

    public boolean isRunning() throws DisJException {
        if (this.graphId == null)
            throw new DisJException(IConstants.ERROR_18);
       
        if (this.proc == null)
            throw new DisJException(IConstants.ERROR_19);

        if(this.proc.isStop())
            this.terminate();
        
        return (this.started && !this.proc.isPause());
    }

    public int getSpeed() throws DisJException{
    	 if (this.proc == null)
             throw new DisJException(IConstants.ERROR_19);

        return this.proc.getSpeed();
    }

    public void setSpeed(int speed)throws DisJException {
    	 if (this.proc == null)
             throw new DisJException(IConstants.ERROR_19);

    	this.proc.setSpeed(speed);
    }
    
    public void setOutputLocation(URL location){
        this.outLocation = location;
    }
 
	static Serializable deepClone(Serializable object) throws DisJException, IOException {
			if (object == null)
				return null;
	
			Serializable obj;
			ObjectOutputStream oos = null;
			ObjectInputStream ois = null;
			try {			
				ClassLoader ld = object.getClass().getClassLoader();
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(bos);
				oos.writeObject(object);
				oos.flush();
				
				ByteArrayInputStream bin = new ByteArrayInputStream(bos
						.toByteArray());
				ois = new CustomObjectInputStream(bin, ld);				
				obj = (Serializable)ois.readObject();
				
				return obj;
				
			} catch (Exception ie) {			
				throw new DisJException(ie);
				
			} finally {
				if (oos != null)
					oos.close();
				if (ois != null)
					ois.close();
			}
	}

}