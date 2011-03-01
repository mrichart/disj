package distributed.plugin.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import distributed.plugin.core.IConstants;

public class ClientClassInputDialog extends Dialog {

	private Button msgPassing;
	private Button boardAgent;
	private Button boardToken;
	private Text   className;
	private String value;
	
	public ClientClassInputDialog(Shell parentShell) {
		super(parentShell);
		this.value = null;
	}
	
	protected void cancelPressed() {
		setReturnCode(-1);
		close();
	}

	protected void configureShell(Shell newShell) {
		newShell.setText("Set Distributed Model Protocol");
		super.configureShell(newShell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		this.msgPassing = new Button(composite, SWT.RADIO);
		this.msgPassing.setText("Message Passing Model");
		this.msgPassing.setSelection(true);

		this.boardAgent = new Button(composite, SWT.RADIO);
		this.boardAgent.setText("Agent with Whiteboard Model");

		this.boardToken = new Button(composite, SWT.RADIO);
		this.boardToken.setText("Agent with Token Model");
		
		this.className = new Text(composite, SWT.BORDER);
		this.className.setText("Fully Qualified Java Class Name");
		this.className.setSize(70, 25);
		
		return composite;
	}

	protected void okPressed() {
		int returnCode = -1;
		if (this.msgPassing.getSelection()){
			returnCode = IConstants.MODEL_MESSAGE_PASSING;
		}else if (this.boardAgent.getSelection()){
			returnCode = IConstants.MODEL_AGENT_WHITEBOARD;
		}else if (this.boardToken.getSelection()){
			returnCode = IConstants.MODEL_AGENT_TOKEN;
		}		
		setReturnCode(returnCode);
		this.value = this.className.getText();
		close();
	}
	
	public String getClassName(){
		return this.value;
	}

}
