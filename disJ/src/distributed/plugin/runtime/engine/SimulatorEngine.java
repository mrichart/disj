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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Graph;
import distributed.plugin.ui.editor.GraphEditor;

/**
 * @author npiyasin
 * 
 * Simulation loader and starter, a bridge between UI and core engine
 */
public class SimulatorEngine {

	private URL outLocation;
    
    private boolean started;

    private int speed;

    private Map<String, Runnable> engine;

    private Graph origin;

    private Thread holder;

	private GraphEditor ge;

    public SimulatorEngine(GraphEditor ge) {
    	this.ge = ge;
        this.speed = IConstants.SPEED_DEFAULT_RATE;
        this.started = false;
        this.engine = new HashMap<String, Runnable>(4);
        this.origin = null;
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
    public void executeMsgPassing(Graph graph, Class<Entity> client, Class<IRandom> clientRandom) {
        try {

           this.origin = graph;
            Runnable proc = new MsgPassingProcessor(graph, client, clientRandom, this.getOutputLocation());
            this.engine.put(origin.getId(), proc);

            // FIXME risk of non atomic for next 2 lines
            this.started = true;
            this.setSpeed(this.speed);
            holder = new Thread(proc, "DisJ Simulator Engine: " + graph.getId());
            holder.start();
            System.out.println("*****[SimulatorEngine] DisJ processors are started*****");

           
        } catch (Exception e) {
        	System.err.println("ERROR @SimultorEngine.execute() " + e);
        }
    }

    public Graph getOriginGraph() {
        return this.origin;
    }

    public boolean isStarted() {
        return started;
    }

    public void terminate() throws DisJException {
        
        this.started = false;
        
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc != null)
            proc.setStop(true);
        
        this.engine.clear();
        
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
        
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);
        proc.setPause(false);
        proc.setStepForward(true);
    }
    

    public void suspend() throws DisJException {
        
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);
        
        proc.setPause(true);
    }

    public boolean isSuspend() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);

        return proc.isPause();
    }

    public void resume() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);

        proc.setPause(false);
        proc.setStepForward(false);
    }

    public boolean isRunning() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);

        if(proc.isStop())
            this.terminate();
        
        return (this.started && !proc.isPause());
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        if (this.origin == null)
            return;
        MsgPassingProcessor proc = (MsgPassingProcessor) this.engine.get(this.origin.getId());
        if (proc != null)           
            proc.setSpeed(speed);
    }
    
    public void setOutputLocation(URL location){
        this.outLocation = location;
    }
    
    public URL getOutputLocation(){
        return this.outLocation;
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