package distributed.plugin.ui.dialogs;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GraphMatrixFileInputDialog extends Dialog {

	private boolean cancel;
	private File value;
	
	public GraphMatrixFileInputDialog(Shell parentShell) {
		super(parentShell);
		setText("Load Matrix File Dialog");
		this.cancel = true;
		this.value = null;
	}
	

	public void open() {
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setSize(320, 200);

		final Text txtResponse = new Text(shell, SWT.BORDER);
		txtResponse.setLocation(10, 10);
		txtResponse.setSize(200, 25);
		txtResponse.setEditable(false);

		// final button
		final Button btnOkay = new Button(shell, SWT.PUSH);
		btnOkay.setText("Ok");
		btnOkay.setLocation(40, 45);
		btnOkay.setSize(100, 30);

		final Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLocation(160, 45);
		btnCancel.setSize(100, 30);
		btnCancel.setSelection(true);
		
		final Button btnBrw = new Button(shell, SWT.PUSH);
		btnBrw.setText("Browse");
		btnBrw.setLocation(220, 10);
		btnBrw.setSize(100, 30);
		btnBrw.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog dialog = new FileDialog(shell, SWT.NULL);
				
				// Set the text
				dialog.setText("Select File");

				// Set filter on .mtx files
				dialog.setFilterExtensions(new String[] { "*.mtx" });

				String path = dialog.open();
				if (path != null) {
					value = new File(path);
					if (value.isFile()){
						txtResponse.setText(value.getAbsolutePath());
					}else{
						txtResponse.setText("It is not a file");
					}
				}
			}
		});

		

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget == btnOkay) {					
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
	
	public boolean isCancel(){
    	return this.cancel;
    }
	
	public File getFile(){
		return this.value;
	}

}
