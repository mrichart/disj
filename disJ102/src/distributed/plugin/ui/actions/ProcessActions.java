/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;

import distributed.plugin.core.DisJException;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.engine.Entity;
import distributed.plugin.runtime.engine.SimulatorEngine;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.SpeedDialog;
import distributed.plugin.ui.editor.GraphEditor;
import distributed.plugin.ui.validators.ClassNameValidator;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class ProcessActions extends WorkbenchPartAction {

	private String execType;
	
	private ClassLoader loader;

//	private GraphElement graphElement;
	
	/**
	 * @param part
	 * @param type
	 *            a type of selected process
	 */
	public ProcessActions(GraphEditor part, String type) {
		super(part);
		this.loader = this.getClassLoader(this.getClientProject());
//		this.graphElement = part.getGraphElement();
		this.execType = type;
		setId(type);
		// System.out.println("processAction created wit type " +
		// this.execType);
	}
	
	private IJavaProject getClientProject(){
		GraphEditor editor = (GraphEditor) getWorkbenchPart();
		IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
		IProject project = input.getFile().getProject();
		String usrProjectName = project.getName();

		IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace()
				.getRoot());
		IJavaProject javaProject = javaModel.getJavaProject(usrProjectName);

		return javaProject;
	}

//	public GraphElement getGraphElement() {
//		return graphElement;
//	}

	/**
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {
		return this.canPerformAction();
	}

	/*
	 * Validate the execution
	 */
	private boolean canPerformAction() {
		// if (getWorkbenchPart() instanceof GraphEditor) {
		// GraphEditor editor = (GraphEditor) getWorkbenchPart();
		// if (editor.getGraphElement() != null
		// && (editor.getClientObject() != null || this.execType
		// .equals(IGraphEditorConstants.LOAD_ID)))
		// return true;
		// }
		// return false;
		return true;
	}

	public void run() {
		
		GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
		
		if (editor.getController() == null){
			editor.setController(new ProtocolExecutionController(this, editor));
		}
		
		
		if (this.execType.equals(IGraphEditorConstants.RESUME_ID)) {
			this.executeRun();
			
		} else if (this.execType.equals(IGraphEditorConstants.LOAD_ID)) {
			this.executeLoad();
			//editor.setController(new ProtocolExecutionController(this, editor));
			
		} else if (this.execType.equals(IGraphEditorConstants.LOAD_RANDOM_ID)) {
			this.executeRandomLoad();
			
		} else if (this.execType.equals(IGraphEditorConstants.SUSPEND_ID)) {
			this.executeSuspend();
			
		} else if (this.execType.equals(IGraphEditorConstants.STOP_ID)) {
			this.executeStop();
			
		} else if (this.execType.equals(IGraphEditorConstants.NEXT_ID)) {
			this.executeStepNext();
			
		} else if (this.execType.equals(IGraphEditorConstants.SPEED_ID)) {
			this.executeSpeed();
			
		} else if (this.execType.equals(IGraphEditorConstants.LOAD_RECORD_ID)) {
			editor.setController(new PlaybackController(this, editor));
			String fileName=openFileDialog();
			if (editor.setRecFile(fileName)){
				showMessageBox(null,"Record Loading Completed");
			}else{
				showMessageBox(null,"File not found");
			}
		} else if (this.execType.equals(IGraphEditorConstants.SAVE_RECORD_ID)) {
			editor.setRecFileNameForSaving(saveFileDialog());
		}

	}
	
	private String openFileDialog() {
		FileDialog dialog = new FileDialog(getWorkbenchPart().getSite().getShell(),SWT.OPEN);

		dialog.setFilterExtensions(new String[]{"*.rec"});
		return dialog.open();
	}
	
	private String saveFileDialog() {
		FileDialog dialog = new FileDialog(getWorkbenchPart().getSite().getShell(),SWT.SAVE);
		return dialog.open();
	}

	public void showMessageBox(String title,String msg){
		Shell parent = getWorkbenchPart().getSite().getShell();
		MessageDialog.openInformation(parent,title, msg);
	}

	SimulatorEngine getEngine() {
		GraphEditor editor = (GraphEditor) getWorkbenchPart();
		return editor.getEngine();
	}

	/*
	 * Run a simulator action
	 */
	private void executeRun() {
		GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
		IController controller = editor.getController();
		controller.executeRun();
	}

	void missUseActionMsg(String msg) {
		MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
				"Miss use of action", msg);
	}

	boolean validateSavedGraph(GraphEditor editor) {
		if (editor.isDirty()) {
			MessageDialog messageBox;
			Shell parent = getWorkbenchPart().getSite().getShell();
			String[] btnText = { "No", "Cancel", "Yes" };
			messageBox = new MessageDialog(parent, "Save", null,
					"Graph has been modified. Do you want to save it?",
					MessageDialog.QUESTION, btnText, 2);
			messageBox.open();
			int ans = messageBox.getReturnCode();
			if (ans == 0)
				return true;
			else if (ans == 1)
				return false;
			else if (ans == 2) {
				// TODO might need to put progress monitor
				IProgressMonitor monitor = null;
				editor.doSave(monitor);
				return true;
			} else
				return false;

		} else
			return true;
	}

	/*
	 * Load client's algorithm into the engine action
	 */
	private void executeLoad(String protocol) {

		String className = protocol;

		GraphEditor editor = (GraphEditor) getWorkbenchPart();		

		if (loader == null){
			this.loader = this.getClassLoader(this.getClientProject());
			if(loader == null){
				System.err.println("***Bugged*** Cannot instantiate classloader");
			}
		}
		// open Dialog
		Shell parent = getWorkbenchPart().getSite().getShell();

		try {
			// kill current running or pending process if exist
			this.getEngine().terminate();

		} catch (DisJException ignore) {
		}

		try {
			Class<Entity> client = this.loadClientClass(parent, className);

			if (client == null)
				return;

			// load client's class into editor
			editor.setClientObject(client);

		} catch (ClassNotFoundException e) {
			MessageDialog.openError(parent, "Unable to load client Classd", e
					.getMessage()
					+ " not found");

		} catch (Exception g) {
			// unexpected error
			String msg = g + "";
			MessageDialog.openError(parent,
					"Unexpected Load Client Class Error", msg);
		}

	}

	private void executeLoad() {
		System.out.println("--- executeLoad");

		Shell parent = getWorkbenchPart().getSite().getShell();
		GraphEditor editor = (GraphEditor) getWorkbenchPart();
		String className = "Fully Qualified Class Name";

		if (editor.getClientObject() != null)
			className = editor.getClientObject().getName();

		InputDialog classNameDialog = new InputDialog(parent,
				"Java Algorithm Input Dialog", "Class Name", className,
				new ClassNameValidator());
		classNameDialog.open();

		// user pressed OK
		if (classNameDialog.getReturnCode() == 0) {
			className = classNameDialog.getValue();
			executeLoad(className);
			editor.getGraphElement().getGraph().setProtocol(className);
		} else {
			// user press cancel
			return;
		}
	}

	private void executeRandomLoad() {
		System.out.println("--- executeRandomLoad");

		GraphEditor editor = (GraphEditor) getWorkbenchPart();
		
		if (loader == null){
			this.loader = this.getClassLoader(this.getClientProject());
			if(loader == null){
				System.err.println("***Bugged*** Cannot instantiate classloader");
			}
		}
		// open Dialog
		Shell parent = getWorkbenchPart().getSite().getShell();
		String className = "Fully Qualified Class Name";

		if (editor.getClientRandomObject() != null)
			className = editor.getClientRandomObject().getName();

		InputDialog classNameDialog = new InputDialog(parent,
				"Random Number generator Input Dialog", "Class Name",
				className, new ClassNameValidator());
		classNameDialog.open();

		// user pressed OK
		if (classNameDialog.getReturnCode() == 0) {
			className = classNameDialog.getValue();

			try {
				// kill current running or pending process if exist
				this.getEngine().terminate();

			} catch (DisJException ignore) {
			}

			try {
				Class<IRandom> clientRandom = this.loadClientRandomClass(parent,
						className);

				if (clientRandom == null)
					return;

				// load client's class into editor
				editor.setClientRandomObject(clientRandom);

			} catch (ClassNotFoundException e) {
				MessageDialog.openError(parent, "Unable to load client Classd",
						e.getMessage() + " not found");

			} catch (Exception g) {
				// unexpected error
				String msg = g + "";
				MessageDialog.openError(parent,
						"Unexpected Load Client Class Error", msg);
			}

		} else {
			// user press cancel
			return;
		}
	}

	/*
	 * @param javaProject @return
	 */
	private ClassLoader getClassLoader(IJavaProject javaProject) {
		ClassLoader loader = null;
		try {
			System.out.println("[ProcessActions].getClassLoader()");
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			List<URL> urls = new ArrayList<URL>(entries.length);
			for (int i = 0; i < entries.length; i++) {
				IPath classpathEntryPath = entries[i].getPath();
				File classpathEntryFile = null;
				switch (entries[i].getEntryKind()) {
					case IClasspathEntry.CPE_SOURCE:
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
								.getRoot();
						IPath out = root.getProject(
								classpathEntryPath.lastSegment()).getLocation();
						if (out != null)
							classpathEntryFile = out.toFile();
						else
							classpathEntryFile = root.getFolder(
									javaProject.getOutputLocation()).getLocation()
									.toFile();
	
						try {
							URI uri = classpathEntryFile.toURI();
							urls.add(uri.toURL());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						break;
					case IClasspathEntry.CPE_CONTAINER:
						break;
	
					// FIXME Must handle 2 more cases to handle the location of 
					// client source code
				}
			}
			System.out.println("[ProcessActions].getClassLoader()" + urls);

			// set the output file location w.r.t 1st urser src code location
			this.getEngine().setOutputLocation((URL) urls.get(0));
			
			// create class loader
			loader = new URLClassLoader((URL[]) urls.toArray(new URL[urls
					.size()]), ProcessActions.class.getClassLoader());
			
			// set class loader
			GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
			editor.setLoader(loader);
			
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		return loader;
	}

	/**
	 * @param parent
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<Entity> loadClientClass(Shell parent,
			String className) throws ClassNotFoundException {
		Class client = this.loader.loadClass(className);

		if (!Entity.class.isAssignableFrom(client)) {
			MessageDialog.openError(parent, "Load Client Class Error",
					"Class must extends Entity");
			client = null;
		}
		return client;
	}

	private Class<IRandom> loadClientRandomClass(Shell parent,
			String className) throws ClassNotFoundException {
		Class clientRandom = this.loader.loadClass(className);

		if (!IRandom.class.isAssignableFrom(clientRandom)) {
			MessageDialog.openError(parent,
					"Load Random number generator Class Error",
					"Class must extends IRandom");
			clientRandom = null;
		}
		return clientRandom;
	}

	private void executeStop() {
		GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
		IController controller=editor.getController();
		controller.executeStop();
	}

	private void executeSuspend() {
		GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
		IController controller=editor.getController();
		controller.executeSuspend();
	}

	private void executeStepNext() {
		// MessageDialog.openInformation(getWorkbenchPart().getSite().getShell(),
		// "Step Next", "This feature is not yet supported");
		// implemented by Russell Dec 2008
		GraphEditor editor = (GraphEditor) this.getWorkbenchPart();
		IController controller=editor.getController();
		controller.executeStepNext();
	}

	private void executeSpeed() {
		SpeedDialog dialog = new SpeedDialog(getWorkbenchPart().getSite()
				.getShell(), this.getEngine().getSpeed());
		int newSpeed = dialog.open();
		this.getEngine().setSpeed(newSpeed);
		System.out.println("--- executeSpeed with speed: " + newSpeed);
	}

	public boolean validateAction(String commandType) {

		GraphEditor editor = (GraphEditor) getWorkbenchPart();
		Graph graph = editor.getGraphElement().getGraph();

		if (commandType == IGraphEditorConstants.STOP_ID
				|| commandType == IGraphEditorConstants.SUSPEND_ID) {
			if (editor.getClientObject() == null
					&& graph.getProtocol().equals("")) {
				MessageDialog.openError(
						getWorkbenchPart().getSite().getShell(),
						"Algoithm has not been loaded",
						"Try to load algorithm before execute this action");
				return false;
			}
		}

		if (commandType == IGraphEditorConstants.RESUME_ID) {
			if (editor.getClientObject() == null
					&& graph.getProtocol().equals("")) {
				MessageDialog.openError(
						getWorkbenchPart().getSite().getShell(),
						"Algoithm has not been loaded",
						"Try to load algorithm before execute this action");
				return false;
			} else {
				if (!graph.getProtocol().equals("")) {
					executeLoad(graph.getProtocol());
				}
			}
		}
		// defaule return is true;
		return true;
	}
}
