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
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class GenericGraphCDialog extends Dialog {

	private int numNode;
	private int numLink;
	private int numInitiators;
	private String linkType;

	/**
	 * @param arg0
	 */
	public GenericGraphCDialog(Shell arg0) {
		super(arg0);
		setText("Arbitrary Graph Dialog");
		this.numNode = 0;
		this.linkType = null;
	}

	public void open() {

		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(300, 200);

		// number of node
		Label numNodeQs = new Label(shell, SWT.NONE);
		numNodeQs.setLocation(25, 10);
		numNodeQs.setSize(100, 25);
		numNodeQs.setText("Num of Node: ");

		final Text txtResponse = new Text(shell, SWT.BORDER);
		txtResponse.setLocation(130, 10);
		txtResponse.setSize(40, 25);

		// number of links
		Label numNodeQl = new Label(shell, SWT.NONE);
		numNodeQl .setLocation(25, 40);
		numNodeQl .setSize(100, 25);
		numNodeQl .setText("Num of Links: ");

		final Text txtLinkResponse = new Text(shell, SWT.BORDER);
		txtLinkResponse.setLocation(130, 40);
		txtLinkResponse.setSize(40, 25);
		
		// number of initiators
		final Label numIts = new Label(shell, SWT.NONE);
		numIts.setLocation(25, 70);
		numIts.setSize(100, 25);
		numIts.setText("Num of Init: ");

		final Text txtInitResponse = new Text(shell, SWT.BORDER);
		txtInitResponse.setLocation(130, 70);
		txtInitResponse.setSize(40, 25);

		// link type
//		Label direct = new Label(shell, SWT.NONE);
//		direct.setLocation(25, 70);
//		direct.setSize(80, 25);
//		direct.setText("Type of Link: ");
//
//		final Combo type = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
//		type.setItems(new String[] { IGraphEditorConstants.UNI,
//				IGraphEditorConstants.BI });
//		type.select(1);
//		type.setLocation(105, 70);
//		type.setSize(150, 25);

		// final button
		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Ok");
		btnOkay.setLocation(40, 120);
		btnOkay.setSize(100, 30);

		final Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLocation(160, 120);
		btnCancel.setSize(100, 30);
		btnCancel.setSelection(true);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget == btnOkay) {
					String res = validateInput(txtResponse.getText());
					if (res != null) {
						MessageDialog.openError(getParent(), "Invalid Input",
								res);
						return;
					} else {
						numNode = Integer
								.parseInt(txtResponse.getText().trim());
					}
					
					res = validateInputLinkNumber(txtResponse.getText(),txtLinkResponse.getText());
					if (res != null) {
						MessageDialog.openError(getParent(), "Invalid Input",
								res);
						return;
					} else {
						numLink = Integer
								.parseInt(txtLinkResponse.getText().trim());
					}
					
					numInitiators= Integer	.parseInt(txtInitResponse.getText().trim());
					
//					linkType = type.getText();
					linkType =IGraphEditorConstants.BI;
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

	private String validateInput(Object param) {
		try {
			int i = Integer.parseInt(((String) param).trim());
			if (i < 2)
				return "The size of graph should more than 1";
			else
				return null;
		} catch (NumberFormatException n) {
			return "Input must be an integer number";
		}

	}
	
	private String validateInputLinkNumber(Object nodes,Object links) {
		try {
			int n = Integer.parseInt(((String) nodes).trim());
			int l = Integer.parseInt(((String) links).trim());
			if (l>(n)*(n-1)/2)
				return "Too many links";
			else
				return null;
		} catch (NumberFormatException n) {
			return "Input must be an integer number";
		}

	}

	public String getLinkType() {
		return linkType;
	}

	public int getNumNode() {
		return numNode;
	}
	
	public int getNumLink() {
		return numLink;
	}

	public int getNumInitiators() {
		return numInitiators;
	}
	
	
}
