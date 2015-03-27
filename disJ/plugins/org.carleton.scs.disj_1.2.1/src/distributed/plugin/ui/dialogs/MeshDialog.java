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
public class MeshDialog extends Dialog {

	private boolean cancel;	
	private boolean isOriented;
    private int numRows;
    private int numCols;
    private int numInit;
    private String linkType;

    /**
     * @param arg0
     */
    public MeshDialog(Shell arg0) {
        super(arg0);
        setText("Mesh Dialog");
        this.cancel = true;
        this.isOriented = false;
        this.numRows = 0;
        this.numCols = 0;
        this.linkType = null;
    }

    public void open() {

        final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        shell.setSize(320, 220);

        // number of row
        Label numNodeQs = new Label(shell, SWT.NONE);
        numNodeQs.setLocation(25, 10);
        numNodeQs.setSize(160, 25);
        numNodeQs.setText("Number of row: ");

        final Text txtResponse = new Text(shell, SWT.BORDER);
        txtResponse.setLocation(190, 10);
        txtResponse.setSize(50, 25);

        // number of column
        Label numDiaQs = new Label(shell, SWT.NONE);
        numDiaQs.setLocation(25, 40);
        numDiaQs.setSize(160, 25);
        numDiaQs.setText("Number of column: ");

        final Text diaRes = new Text(shell, SWT.BORDER);
        diaRes.setLocation(190, 40);
        diaRes.setSize(50, 25);

        // number of initiators
		final Label numIts = new Label(shell, SWT.NONE);
		numIts.setLocation(25, 70);
		numIts.setSize(160, 25);
		numIts.setText("Num of Init: ");

		final Text txtInitResponse = new Text(shell, SWT.BORDER);
		txtInitResponse.setLocation(190, 70);
		txtInitResponse.setSize(50, 25);
		
        // link type
        Label direct = new Label(shell, SWT.NONE);
        direct.setLocation(25, 100);
        direct.setSize(100, 25);
        direct.setText("Type of Link: ");

        final Combo type = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
        type.setItems(new String[] { IGraphEditorConstants.UNI, IGraphEditorConstants.BI });
        type.select(1);
        type.setLocation(130, 100);
        type.setSize(150, 25);

        // orientation type
        final Button box = new Button(shell, SWT.CHECK);
        box.setText(" Oriented");
        box.setSelection(true);
        box.setLocation(25, 130);
        box.setSize(160, 25);
        
        // final button
        final Button btnOkay = new Button(shell, SWT.PUSH);
        btnOkay.setText("Ok");
        btnOkay.setLocation(40, 160);
        btnOkay.setSize(100, 30);

        final Button btnCancel = new Button(shell, SWT.PUSH);
        btnCancel.setText("Cancel");
        btnCancel.setLocation(160, 160);
        btnCancel.setSize(100, 30);
        btnCancel.setSelection(true);

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                if (event.widget == btnOkay) {
                    // get number of rows
                    String res = validateRowsInput(txtResponse.getText());
                    if (res != null) {
                        MessageDialog.openError(getParent(), "Invalid Input",
                                res);
                        return;
                    } else {
                        numRows = Integer
                                .parseInt(txtResponse.getText().trim());
                    }

                    // get number of columns
                    res = validateColsInput(diaRes.getText().trim());
                    if (res != null) {
                        MessageDialog.openError(getParent(), "Invalid Input",
                                res);
                        return;
                    } else {
                        numCols = Integer.parseInt(diaRes.getText().trim());
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
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    private String validateRowsInput(Object param) {
        try {
            int i = Integer.parseInt(((String) param).trim());
            if (i < 2)
                return "The row must more than 1";
            else
                return null;
        } catch (NumberFormatException n) {
            return "Input must be an integer number";
        }
    }

    private String validateColsInput(Object param) {
        try {
            int i = Integer.parseInt(((String) param).trim());
            if (i < 2)
                return "The colum must more than 1";
            else
                return null;
        } catch (NumberFormatException n) {
            return "Input must be an integer number";
        }

    }

    private String validateInitInput(Object param){
        try{
            int i = Integer.parseInt(((String)param).trim());
            if(i > this.numCols * this.numRows)
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
        return this.linkType;
    }

    public int getNumRows() {
        return this.numRows;
    }

    public int getNumCols() {
        return this.numCols;
    }
    
    public boolean isCancel(){
    	return this.cancel;
    }
    public boolean isOriented(){
        return this.isOriented;
    }
    
}
