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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class RemoveStatesDialog extends Dialog {

	private Map<Integer, RGB> stateColr;

	private boolean response;
	
	/**
	 * @param arg0
	 */
	public RemoveStatesDialog(Shell arg0, Map<Integer, RGB> stateColr) {
		super(arg0);
		setText("View/Remove State Dialog");
		this.stateColr = stateColr;
		this.response = false;
	}

	public List<Short> open() {
		
		final List<Short> remList = new ArrayList<Short>();
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(250, 300);

		// a table list
		final Table table2 = new Table(shell, SWT.CHECK | SWT.HIDE_SELECTION);
		table2.setBounds(10, 10, 200, 150);
		table2.setHeaderVisible(true);
		

		TableColumn col = new TableColumn(table2, SWT.LEFT);
		col.setText("State");
		col.setWidth(40);
		TableColumn colour = new TableColumn(table2, SWT.LEFT);
		colour.setText("Color");
		colour.setWidth(50);
		
		// display existing colors
		List<Integer> temp = new ArrayList<Integer>();
		for (Integer state : this.stateColr.keySet()) {
			temp.add(state);
		}
		Collections.sort(temp);
		for (Integer num : temp) {
			// FIXME why is 99???? use constant!!
			if (num != 99) {
				Color color = new Color(getParent().getDisplay(),
						stateColr.get(num));
				TableItem e = new TableItem(table2, SWT.NONE);
				e.setText(0, num.toString());
				e.setBackground(1, color);
			}
		}

		// final button
		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Remove");
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
					response = true;
					TableItem[] items = table2.getItems();
					for (int i = 0; i < items.length; i++) {
						if (items[i].getChecked()) {							
							String state = items[i].getText(0);
							short s = Short.parseShort(state);
							remList.add(s);
						}
					}
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
		
		if (response)
			return remList;
		else
			return null;
	}

}
