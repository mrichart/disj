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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AddStatesDialog extends Dialog {

    // a pair of state and color {Short, RGB};
    private Object[] states;

    private boolean response;

    /**
     * @param arg0
     */
    public AddStatesDialog(Shell arg0) {
        super(arg0);
        setText("Add State Color Dialog");
        this.states = new Object[2];
    }

    public Object[] open() {

        final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        shell.setSize(300, 200);

        // state number
        Label lblQuestion = new Label(shell, SWT.NONE);
        lblQuestion.setLocation(25, 10);
        lblQuestion.setSize(100, 25);
        lblQuestion.setText("State(number): ");

        final Text txtResponse = new Text(shell, SWT.BORDER);
        txtResponse.setLocation(130, 10);
        txtResponse.setSize(50, 25);

        // state color
        Label colr = new Label(shell, SWT.NONE);
        colr.setLocation(25, 50);
        colr.setSize(80, 30);
        colr.setText("State Color: ");

        final Label colRes = new Label(shell, SWT.BORDER);
        colRes.setText("");
        colRes.setLocation(110, 50);
        colRes.setSize(30, 30);

        Button colBtn = new Button(shell, SWT.PUSH);
        colBtn.setText("Set Color");
        colBtn.setLocation(145, 50);
        colBtn.setSize(130, 30);

        // final button
        final Button btnOkay = new Button(shell, SWT.PUSH);
        btnOkay.setText("Ok");
        btnOkay.setLocation(40, 100);
        btnOkay.setSize(100, 30);

        final Button btnCancel = new Button(shell, SWT.PUSH);
        btnCancel.setText("Cancel");
        btnCancel.setLocation(160, 100);
        btnCancel.setSize(100, 30);
        btnCancel.setSelection(true);

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                response = (event.widget == btnOkay);
                states[0] = txtResponse.getText();
                shell.close();
            }
        };

        Listener colrListerner = new Listener() {
            public void handleEvent(Event event) {
                ColorDialog colorDialog = new ColorDialog(shell);
                colorDialog.setText("Color Palette");
                colorDialog.setRGB(new RGB(255, 0, 0));
                RGB color = colorDialog.open();
                colRes.setBackground(new Color(getParent().getDisplay(), color));
                states[1] = color;
                btnOkay.setSelection(true);
                btnCancel.setSelection(false);
            }
        };
        
        btnOkay.addListener(SWT.Selection, listener);
        btnCancel.addListener(SWT.Selection, listener);
        colBtn.addListener(SWT.Selection, colrListerner);

        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        if (response)
            return states;
        else
            return new Object[0];
    }
}
