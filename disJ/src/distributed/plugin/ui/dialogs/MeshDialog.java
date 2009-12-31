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

    private int numRows;

    private int numCols;

    private String linkType;

    /**
     * @param arg0
     */
    public MeshDialog(Shell arg0) {
        super(arg0);
        setText("Mesh Dialog");
        this.numRows = 0;
        this.numCols = 0;
        this.linkType = null;
    }

    public void open() {

        final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        shell.setSize(320, 200);

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

        // link type
        Label direct = new Label(shell, SWT.NONE);
        direct.setLocation(25, 70);
        direct.setSize(100, 25);
        direct.setText("Type of Link: ");

        final Combo type = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
        type.setItems(new String[] { IGraphEditorConstants.UNI, IGraphEditorConstants.BI });
        type.select(1);
        type.setLocation(130, 70);
        type.setSize(150, 25);

        // final button
        final Button btnOkay = new Button(shell, SWT.PUSH);
        btnOkay.setText("Ok");
        btnOkay.setLocation(40, 140);
        btnOkay.setSize(100, 30);

        final Button btnCancel = new Button(shell, SWT.PUSH);
        btnCancel.setText("Cancel");
        btnCancel.setLocation(160, 140);
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

                    // get number of colums
                    res = validateColsInput(diaRes.getText().trim());
                    if (res != null) {
                        MessageDialog.openError(getParent(), "Invalid Input",
                                res);
                        return;
                    } else {
                        numCols = Integer.parseInt(diaRes.getText().trim());
                    }
                    linkType = type.getText();
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

    public String getLinkType() {
        return this.linkType;
    }

    public int getNumRows() {
        return this.numRows;
    }

    public int getNumCols() {
        return this.numCols;
    }
}
