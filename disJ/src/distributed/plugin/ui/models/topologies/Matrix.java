/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models.topologies;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.GraphMatrixFileInputDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 *
 */
public class Matrix extends AbstractGraph {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE * 3;

    private File path;
    
    private int[][] matrix;
    
    private boolean isCancel;

    /**
     * Constructor;
     */
    public Matrix(GraphElementFactory factory, Shell shell) {
    	super(factory, shell);
    	this.path = null; 
    	this.matrix = null;
    	this.isCancel = true;
      
    }

    public int[][] getMatrix(){
    	return this.matrix;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology(int)
     */
    public void createTopology() {  	
    	GraphMatrixFileInputDialog dialog = new GraphMatrixFileInputDialog(this.shell);
        dialog.open();
        
        if(!dialog.isCancel()){
        	this.isCancel = false;
	        this.path = dialog.getFile();
	        try{
	        	if(this.path == null){
	        		throw new NullPointerException("Input file is null");
	        	}
	        	this.constructMatrix();
	        	this.numNode = this.matrix[0].length;
	        	this.createNodes();
	        	
	        }catch (IOException e) {
	        	this.isCancel = true;
	        	MessageDialog.openWarning(this.shell, "Error", 
	        			String.format("File %s Not foud or Invalid file", this.path.getName()));
	        	
			}catch (NumberFormatException e){
				this.isCancel = true;
				MessageDialog.openWarning(this.shell, "Error", 
	        			String.format("File %s must contain only integer matrix", this.path.getName()));
	        	
			} catch (NullPointerException e){
				this.isCancel = true;
				MessageDialog.openWarning(this.shell, "Error", 
	        			String.format("File path not found: %s", e.toString()));
	        	
			} catch (Exception e){
				this.isCancel = true;
				MessageDialog.openWarning(this.shell, "Error", 
	        			String.format("File %s contains invalid format", this.path.getName()));
	        	
			}	        
        }
    }
    
    /**
     * Construct a matrix from a text file in matrix format.
     * The example of a ring of 3 nodes: 
     * 			0,1,1
     * 			1,0,1
     * 			1,1,0 
     * The zero value mean there is no connection between node of
     * column c and row r. In this case a node cannot connect to
     * itself.
     * 
     * @throws IOException
     */
    
	private void constructMatrix() throws IOException {
		FileInputStream fstream = new FileInputStream(this.path);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader buff = new BufferedReader(new InputStreamReader(in));
		String line;
		List<String[]> list= new ArrayList<String[]>();
		while ((line = buff.readLine()) != null) {
			list.add(line.split(","));
		}
		
		if(list.size() > 0){
			this.matrix = new int[list.size()][list.size()];
			for (int k = 0; k < list.size(); k++) {
				String[] row = list.get(k);
				int[] r = new int[row.length];
				for(int i = 0; i < row.length; i++){
					r[i] = Integer.parseInt(row[i]);
				}
				this.matrix[k] = r;
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private void createNodes(){
        for (int i = 0; i < this.numNode; i++) {
            this.nodes.add(this.factory.createNodeElement());
        }
	}
    
    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getConnectionType()
     */
    public String getConnectionType() {
        return IGraphEditorConstants.BI;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#applyLocation(org.eclipse.draw2d.geometry.Point)
     */
    public void applyLocation(Point point) {
    	if(this.isCancel){
    		return;
    	}
    	
    	int col = (int) Math.round(Math.sqrt(this.numNode));
    	
    	int x = point.x;
        int y = point.y;        
        int px, py;
        int k = 0;
        int r = 0;
        NodeElement node;
        while (k < this.nodes.size()) {
        	for (int i = 0; i < col; i++) {
        		if(k < this.nodes.size()){
	                node = this.nodes.get(k);
	                px = x + (i * GAP);
	                py = y + (r * GAP);           	            
	                Point p = new Point(px, py);                              
	                node.setLocation(p);
	                node.setSize(new Dimension(
	                        IGraphEditorConstants.NODE_SIZE,
	                        IGraphEditorConstants.NODE_SIZE));
	                k++;
                } else {
                	break;
                }
            }
        	r++;
		}    	
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_MATRIX_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
    	if(this.isCancel){
    		return;
    	}
    	
    	// first row
    	int[] header = this.matrix[0];
    	NodeElement source = null;
		for(int k = 0; k < header.length; k++){	
			// get source
			source = (NodeElement)this.nodes.get(k);
			
			// connect to targets of the same column of the source node
			NodeElement target = null;
			LinkElement link = null;
			for(int row = k+1; row < this.matrix.length; row++){
				if(this.matrix[row][k] > 0){
					// get target connect to this source node
					target = (NodeElement)this.nodes.get(row);
					
					// always bi-directional link
					link = this.factory.createBiLinkElement();
					
					// connect 2 nodes
					link.setSource(source);
					link.attachSource();
					link.setTarget(target);
			        link.attachTarget();			 			       
	            	
			        // add to tracker list
					this.links.add(link);					
				}										
			}			
		}   	
    }


}
