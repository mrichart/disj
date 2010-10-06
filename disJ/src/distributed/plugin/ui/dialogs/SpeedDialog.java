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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SpeedDialog extends Dialog {

	private boolean response;
	private int speed;

	/**
	 * @param arg0
	 */
	public SpeedDialog(Shell arg0, int speed) {
		super(arg0);
		setText("Process Speed Dialog");
		this.speed = speed;
	}

	public int open() {
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(150, 220);

		// state color
		Label colr = new Label(shell, SWT.NONE);
		colr.setLocation(10, 10);
		colr.setSize(150, 20);
		colr.setText(IGraphEditorConstants.SPEED);

		// final Scale speedScale = new Scale(shell, SWT.HORIZONTAL);
//		 speedScale.setMinimum(IConstants.SPEED_MAX_RATE);
		// speedScale.setMaximum(IConstants.SPEED_MIN_RATE);
		// speedScale.setSelection(this.speed);
		// speedScale.setLocation(10, 35);
		// speedScale.setSize(250, 20);
		// speedScale.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// speed = speedScale.getSelection();
		// }
		// });
		//		
		// Label min = new Label(shell, SWT.NONE);
		// min.setLocation(10, 60);
		// min.setSize(50, 20);
		// min.setText("Fast");
		//        
		// Label max = new Label(shell, SWT.NONE);
		// max.setLocation(220, 60);
		// max.setSize(50, 20);
		// max.setText("Slow");
		
		final int[] speeds=new int[4];
		
		speeds[0]=50;
		speeds[1]=1000;
		speeds[2]=2000;
		speeds[3]=0;
		
		final Button[] radios = new Button[4];
		
		radios[3] = new Button(shell, SWT.RADIO);
		radios[3].setText("2 sec");
		radios[3].setBounds(10, 30, 75, 30);

		radios[0] = new Button(shell, SWT.RADIO);
		radios[0].setText("Fast");
		radios[0].setBounds(10, 60, 75, 30);

		radios[1] = new Button(shell, SWT.RADIO);
		radios[1].setText("Normal");
		radios[1].setBounds(10, 90, 75, 30);

		radios[2] = new Button(shell, SWT.RADIO);
		radios[2].setText("Slow");
		radios[2].setBounds(10, 120, 75, 30);
		

		
		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Ok");
		btnOkay.setLocation(10, 160);
		btnOkay.setSize(100, 30);		
		
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				
				if (event.widget == btnOkay) {
					for (int i=0;i<4;i++){
						if (radios[i].getSelection()){
							speed=speeds[i];
							break;
						}
					}
				}
				shell.close();
			}
		};		
		btnOkay.addListener(SWT.Selection, listener);
		
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return speed;
	}
}
