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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.runtime.Graph;

/**
 * @author npiyasin
 * 
 * Simulation loader and starter, a bridge between UI and core engine
 */
public class SimulatorEngine {

    private URL outLocation;
    
    private boolean started;

    private int speed;

    private Map engine;

    private Graph origin;

    private Thread holder;

    public SimulatorEngine() {
        this.speed = IConstants.SPEED_DEFAULT_RATE;
        this.started = false;
        this.engine = new HashMap(1);
        this.origin = null;
        this.holder = null;
    }

    /**
     * Create the process with a given graphId and client Class
     * 
     * @param graphId
     * @param client
     */
    public void execute(Graph graph, Class client, Class clientRandom) {
        try {

           this.origin = graph;
            Runnable proc = new Processor(graph, client, clientRandom, this.getOutputLocation());
            this.engine.put(origin.getId(), proc);

            // FIXME risk of non atomic for next 2 lines
            this.started = true;
            this.setSpeed(this.speed);
            holder = new Thread(proc, "DisJ Simulator Engine: " + graph.getId());
            holder.start();
            System.out.println("*****[SimulatorEngine] DisJ processors are started*****");

           
        } catch (IOException e) {
        	// TODO all these errors must notify user via SWT widget
            e.printStackTrace();
            System.out.println(e);
        } catch (Exception e) {
        	// TODO all these errors must notify user via SWT widget
            e.printStackTrace();
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

        Processor proc = (Processor) this.engine.get(this.origin.getId());
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

    public void suspend() throws DisJException {
        
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        Processor proc = (Processor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);
        
        proc.setPause(true);
    }

    public boolean isSuspend() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        Processor proc = (Processor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);

        return proc.isPause();
    }

    public void resume() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        Processor proc = (Processor) this.engine.get(this.origin.getId());
        if (proc == null)
            throw new DisJException(IConstants.ERROR_19);

        proc.setPause(false);
    }

    public boolean isRunning() throws DisJException {
        if (this.origin == null)
            throw new DisJException(IConstants.ERROR_18);

        Processor proc = (Processor) this.engine.get(this.origin.getId());
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
        Processor proc = (Processor) this.engine.get(this.origin.getId());
        if (proc != null)           
            proc.setSpeed(speed);
    }
    
    public void setOutputLocation(URL location){
        this.outLocation = location;
    }
    
    public URL getOutputLocation(){
        return this.outLocation;
    }

}