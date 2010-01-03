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

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class AddStatesDialog extends Dialog {

	// a pair of state and color {Short, RGB};

	private Hashtable ht;

	private boolean response;

	/**
	 * @param arg0
	 */
	public AddStatesDialog(Shell arg0) {
		super(arg0);
		setText("Add State Color Dialog");
		ht = new Hashtable();
	}

	public Hashtable open() {

		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(300, 400);

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

		// final Button btnAdd = new Button(shell, SWT.PUSH);
		// btnAdd.setText("Add");
		// btnAdd.setLocation(10, 100);
		// btnAdd.setSize(70, 30);

		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Ok");
		btnOkay.setLocation(80, 100);
		btnOkay.setSize(70, 30);

		final Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLocation(160, 100);
		btnCancel.setSize(70, 30);
		btnCancel.setSelection(true);

		// final Text txtResponseToAdd = new Text(shell, SWT.BORDER);
		// txtResponseToAdd.setLocation(25, 150);
		// txtResponseToAdd.setSize(150, 25);

		final Table colorTable = new Table(shell,SWT.H_SCROLL);
		colorTable.setBounds(10, 160, 200, 200);
		colorTable.setHeaderVisible(true);

		TableColumn col = new TableColumn(colorTable, SWT.CENTER);
		col.setText("State");
		col.setWidth(40);
		TableColumn colour = new TableColumn(colorTable, SWT.LEFT);
		colour.setText("Colour");
		colour.setWidth(50);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				response = (event.widget == btnOkay);
				shell.close();
			}
		};

		Listener colrListerner = new Listener() {
			public void handleEvent(Event event) {
				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setText("Color Palette");
				colorDialog.setRGB(new RGB(255, 0, 0));
				RGB color = colorDialog.open();
				colRes
						.setBackground(new Color(getParent().getDisplay(),
								color));

				// show in color table
				if (!txtResponse.getText().equals("")) {
					ht.put(new Short(txtResponse.getText()), color);
					colorTable.removeAll();
					List keys = new LinkedList(ht.keySet());
					Collections.sort(keys);
					for (Object key : keys) {
						Color aColor = new Color(getParent().getDisplay(),
								(RGB) ht.get(key));
						TableItem e = new TableItem(colorTable, SWT.NONE);
						e.setText(0, key.toString());
						e.setBackground(1, aColor);
					}
				}
				txtResponse.setText("");
				btnOkay.setSelection(true);
				btnCancel.setSelection(false);
			}
		};

		// btnAdd.addListener(SWT.Selection, addListener);
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
			return ht;
		else
			return null;
	}
}
