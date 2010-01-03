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
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import distributed.plugin.ui.editor.GraphEditor;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class RemoveStatesDialog extends Dialog {

	private boolean response;
	private GraphEditor editor;

	/**
	 * @param arg0
	 */
	public RemoveStatesDialog(Shell arg0, GraphEditor editor) {
		super(arg0);
		setText("View/Remove State Dialog");
		this.editor = editor;
	}

	public void open() {
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(250, 250);

		// a table list
		final Table table2 = new Table(shell, SWT.CHECK | SWT.HIDE_SELECTION);
		table2.setBounds(10, 10, 200, 150);
		table2.setHeaderVisible(true);

		TableColumn col = new TableColumn(table2, SWT.LEFT);
		col.setText("State");
		col.setWidth(40);
		TableColumn colour = new TableColumn(table2, SWT.LEFT);
		colour.setText("Colour");
		colour.setWidth(50);

		Iterator its = this.editor.getGraphElement().getStateColors();
		for (Object key = null; its.hasNext();) {
			key = its.next();
			// "99" is reserved for resetting
			if (!key.equals(new Short("99"))) {
				Color color = this.editor.getGraphElement().getStateColor(key);
				TableItem e = new TableItem(table2, SWT.NONE);
				e.setText(0, key.toString());
				e.setBackground(1, color);
			}
		}

		// final button
		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Ok");
		btnOkay.setLocation(20, 170);
		btnOkay.setSize(100, 25);
		btnOkay.setSelection(true);

		Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLocation(130, 170);
		btnCancel.setSize(100, 25);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget == btnOkay) {
					TableItem[] items = table2.getItems();
					boolean modified = false;
					for (int i = 0; i < items.length; i++) {
						if (items[i].getChecked()) {
							modified = true;
							String state = items[i].getText(0);
							editor.getGraphElement().removeStateColor(
									new Short(state));
						}
					}
					if (modified)
						editor.makeDirty();
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

}
