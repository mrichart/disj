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
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.EditorPart;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Node;
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
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProcessActions extends WorkbenchPartAction {

    private String execType;

    /**
     * @param part
     * @param type
     *            a type of selected process
     */
    public ProcessActions(EditorPart part, String type) {
        super(part);
        this.execType = type;
        setId(type);
        //System.out.println("processAction created wit type " + this.execType);
    }

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
        if (this.execType.equals(IGraphEditorConstants.RESUME_ID)) {
            this.executeRun();
        } else if (this.execType.equals(IGraphEditorConstants.LOAD_ID)) {
            this.executeLoad();
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
        }

    }

    private SimulatorEngine getEngine() {
        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        return editor.getEngine();
    }

    /*
     * Run a simulator action
     */
    private void executeRun() {
        if(!this.validateAction())
            return;
        System.out.println("--- executeRun");

        // first run
        if (this.getEngine().isStarted() == false) {
            GraphEditor editor = (GraphEditor) getWorkbenchPart();
            this.validateSavedGraph(editor);
            Graph graph = editor.getGraphElement().getGraph();

            // FIXME a cheated way to refresh the node color
            // to user setting state
            Map nodes = graph.getNodes();
            Iterator its = nodes.keySet().iterator();
            for (Node node = null; its.hasNext();) {
                node = (Node) nodes.get(its.next());
                node.resetState(node.getCurState());
            }
            editor.getGraphElement().copyGraphElement();
            this.getEngine().execute(graph, editor.getClientObject(), editor.getClientRandomObject());
        } else {
            try {
                if ((!this.getEngine().isRunning())
                        && this.getEngine().isStarted()
                        && this.getEngine().isSuspend())
                    this.getEngine().resume();
                else{
                    this.missUseActionMsg("Engine is not suspened or stoped.");
                }
            } catch (DisJException e) {
                System.err.println(e);
            }
        }
    }

    private void missUseActionMsg(String msg){
        MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
                "Miss use of action",
                msg);
    }
    
    private boolean validateAction() {
        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        if (editor.getClientObject() == null){
            MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
                    "Algoithm has not been loaded",
                    "Try to load algorithm before execute this action");
            return false;
        }else{
            return true;
        }
        
    }

    private boolean validateSavedGraph(GraphEditor editor) {
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
    private void executeLoad() {
        System.out.println("--- executeLoad");

        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
        IProject project = input.getFile().getProject();
        String usrProjectName = project.getName();

        IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace()
                .getRoot());
        IJavaProject javaProject = javaModel.getJavaProject(usrProjectName);

        ClassLoader loader = this.getClassLoader(javaProject);

        if (loader == null)
            System.err.println("***Bugged*** Cannot instantiate classloader");

        // open Dialog
        Shell parent = getWorkbenchPart().getSite().getShell();
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

            try {
                // kill current running or pending process if exist
                this.getEngine().terminate();

            } catch (DisJException ignore) {
            }

            try {
                Class client = this.loadClientClass(parent, loader, className);

                if (client == null)
                    return;

                // loas client's class into editor
                editor.setClientObject(client);

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
    
    private void executeRandomLoad() {
        System.out.println("--- executeRandomLoad");

        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
        IProject project = input.getFile().getProject();
        String usrProjectName = project.getName();

        IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace()
                .getRoot());
        IJavaProject javaProject = javaModel.getJavaProject(usrProjectName);

        ClassLoader loader = this.getClassLoader(javaProject);

        if (loader == null)
            System.err.println("***Bugged*** Cannot instantiate classloader");

        // open Dialog
        Shell parent = getWorkbenchPart().getSite().getShell();
        String className = "Fully Qualified Class Name";

        if (editor.getClientRandomObject() != null)
            className = editor.getClientRandomObject().getName();

        InputDialog classNameDialog = new InputDialog(parent,
                "Random Number generator Input Dialog", "Class Name", className,
                new ClassNameValidator());
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
                Class clientRandom = this.loadClientRandomClass(parent, loader, className);

                if (clientRandom == null)
                    return;

                // loas client's class into editor
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
            System.out.println("Enter getClassLoader()");
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            List urls = new ArrayList(entries.length);
            for (int i = 0; i < entries.length; i++) {
                IPath classpathEntryPath = entries[i].getPath();
                File classpathEntryFile = null;
                switch (entries[i].getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE:
                    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                    IPath out = root.getProject(classpathEntryPath.lastSegment()).getLocation();
                    if (out != null)
                        classpathEntryFile = out.toFile();                             
                    else
                        classpathEntryFile = root.getFolder(javaProject.getOutputLocation()).getLocation().toFile();
                    
                    try {
                    	URI uri =  classpathEntryFile.toURI();
                        urls.add(uri.toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    break;
                case IClasspathEntry.CPE_CONTAINER:
                    break;

                // FIXME Must handle 2 more cases
                }
            }
            System.out.println("[ProcessActions].getClassLoade()" + urls);

            // set the output file location w.r.t 1st urser src code location
            this.getEngine().setOutputLocation((URL) urls.get(0));
            // create class loader
            loader = new URLClassLoader((URL[]) urls.toArray(new URL[urls
                    .size()]), ProcessActions.class.getClassLoader());
        } catch (JavaModelException e1) {
            ///e1.printStackTrace();
        }
        return loader;
    }

    /**
     * @param parent
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private Class loadClientClass(Shell parent, ClassLoader loader,
            String className) throws ClassNotFoundException {
        Class client = loader.loadClass(className);

        if (!Entity.class.isAssignableFrom(client)) {
            MessageDialog.openError(parent, "Load Client Class Error",
                    "Class must extends Entity");
            client = null;
        }
        return client;
    }
    
    private Class loadClientRandomClass(Shell parent, ClassLoader loader,
            String className) throws ClassNotFoundException {
        Class clientRandom = loader.loadClass(className);

        if (!IRandom.class.isAssignableFrom(clientRandom)) {
            MessageDialog.openError(parent, "Load Random number generator Class Error",
                    "Class must extends IRandom");
            clientRandom = null;
        }
        return clientRandom;
    }

    private void executeStop() {
        if(!this.validateAction())
            return;
        
        System.out.println("--- executeStop");
        try {
            if (this.getEngine().isStarted())
                this.getEngine().terminate();
            

            if (this.getEngine().getOriginGraph() != null) {
                // reset the states and data of a graph
                GraphEditor editor = (GraphEditor) getWorkbenchPart();
                editor.getGraphElement().resetGraphElement();
            } else {
                this.missUseActionMsg("Engine is not running");
            }

        } catch (DisJException e) {
            //e.printStackTrace();
            //System.err.println(e);
        }

    }

    private void executeSuspend() {
        if(!this.validateAction())
            return;
        
        System.out.println("--- executeSuspend");
        try {
//            if(!this.getEngine().isRunning())
//                this.missUseActionMsg("Engine is not running");
               
            if (!this.getEngine().isSuspend())
                this.getEngine().suspend();
            else{
                this.missUseActionMsg("Engine already suspended");
            }
        } catch (DisJException e) {
            //e.printStackTrace();
            //System.err.println(e);
        }
    }

    private void executeStepNext() {
        MessageDialog.openInformation(getWorkbenchPart().getSite().getShell(),
                "Step Next", "This feature is not yet supported");
    }

    private void executeSpeed() {
        SpeedDialog dialog = new SpeedDialog(getWorkbenchPart().getSite()
                .getShell(), this.getEngine().getSpeed());
        int newSpeed = dialog.open();
        this.getEngine().setSpeed(newSpeed);
        System.out.println("--- executeSpeed with speed: " + newSpeed);
    }
}
