/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RingDialog extends Dialog {

	private boolean cancel;
	private boolean isOriented;
    private int numNode;
    private int numInit;
    private String linkType;
    
    /**
     * @param arg0
     */
    public RingDialog(Shell arg0) {
        super(arg0);
        setText("Ring Dialog");
        this.isOriented = true;
        this.cancel = true;
        this.numNode = 0;
        this.numInit = 0;
        this.linkType = null;
    }

    public void open() {

        final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        shell.setSize(320, 200);

        // number of node
        Label numNodeQs = new Label(shell, SWT.NONE);
        numNodeQs.setLocation(25, 10);
        numNodeQs.setSize(160, 25);
        numNodeQs.setText("Number of node: ");

        final Text txtResponse = new Text(shell, SWT.BORDER);
        txtResponse.setLocation(190, 10);
        txtResponse.setSize(50, 25);

        // number of initiators
		final Label numIts = new Label(shell, SWT.NONE);
		numIts.setLocation(25, 40);
		numIts.setSize(160, 25);
		numIts.setText("Num of Init: ");

		final Text txtInitResponse = new Text(shell, SWT.BORDER);
		txtInitResponse.setLocation(190, 40);
		txtInitResponse.setSize(50, 25);
				
        // link type
        Label direct = new Label(shell, SWT.NONE);
        direct.setLocation(25, 70);
        direct.setSize(100, 25);
        direct.setText("Type of Link: ");

        final Combo type = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
        type.setItems(new String[] {IGraphEditorConstants.UNI, IGraphEditorConstants.BI});
        type.select(1);
        type.setLocation(130, 70);
        type.setSize(160, 25);
        
        // orientation type
        final Button box = new Button(shell, SWT.CHECK);
        box.setText(" Oriented");
        box.setSelection(true);
        box.setLocation(25, 100);
        box.setSize(160, 25);

        
        // final button
        final Button btnOkay = new Button(shell, SWT.PUSH);
        btnOkay.setText("Ok");
        btnOkay.setLocation(40, 130);
        btnOkay.setSize(100, 30);

        final Button btnCancel = new Button(shell, SWT.PUSH);
        btnCancel.setText("Cancel");
        btnCancel.setLocation(160, 130);
        btnCancel.setSize(100, 30);
        btnCancel.setSelection(true);
        

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                if(event.widget == btnOkay){
                	// read number of node input
                    String res = validateInput(txtResponse.getText());
                    if(res != null){
                        MessageDialog.openError(getParent(), "Invalid Input",
                                res);
                        return;
                    } else {
                        numNode = Integer.parseInt(txtResponse.getText().trim());
                    }
                    
                    // read number of init input
                    res = validateInitInput(txtInitResponse.getText());
                    if(res != null){
                        MessageDialog.openError(getParent(), "Invalid Input",
                                res);
                        return;
                    } else {
                    	numInit = Integer.parseInt(txtInitResponse.getText().trim());
                    }                                  
                    
                    linkType = type.getText();
                    isOriented = box.getSelection();
                    cancel = false;
                }                
                shell.close();
            }
        };
        
        btnOkay.addListener(SWT.Selection, listener);
        btnCancel.addListener(SWT.Selection, listener);

        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
    
    private String validateInput(Object param){
        try{
            int i = Integer.parseInt(((String)param).trim());
            if(i < 2)
                return "The size of ring should more than 1";
            else
                return null;
        }catch(NumberFormatException n){
            return "Input must be an integer number";
        }       
    }
    
    private String validateInitInput(Object param){
        try{
            int i = Integer.parseInt(((String)param).trim());
            if(i > this.numNode)
                return "The number of init node cannot be more than number of node";
            else if(i < 0)
            	return "Number of init node cannot be negative";
            else
                return null;
        }catch(NumberFormatException n){
            return "Input must be an integer number";
        }      
    } 
    public int getNumInit(){
    	return this.numInit;
    }
    public String getLinkType() {
        return linkType;
    }
    public int getNumNode() {
        return numNode;
    }
   
    public boolean isCancel(){
    	return this.cancel;
    }
    
    public boolean isOriented(){
        return this.isOriented;
    }
    
}
