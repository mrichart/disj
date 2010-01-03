/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer; //import org.eclipse.gef.GEFPlugin;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.CopyTemplateAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.StackAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import distributed.plugin.runtime.engine.SimulatorEngine;
import distributed.plugin.ui.GraphEditorPlugin;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.actions.Controller;
import distributed.plugin.ui.actions.ProcessActions;
import distributed.plugin.ui.actions.ProtocolExecutionController;
import distributed.plugin.ui.actions.StateSettingAction;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.parts.GraphEditPartFactory;

/**
 * A graph editor for drawing topology
 * 
 */
public class GraphEditor extends GraphicalEditorWithFlyoutPalette {

	protected static final String PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$

	protected static final String PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$

	protected static final String PALETTE_STATE = "Palette state"; //$NON-NLS-1$

	protected static final int DEFAULT_PALETTE_SIZE = 130;

	static {
		GraphEditorPlugin.getDefault().getPreferenceStore().setDefault(
				PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
	}

	private SimulatorEngine engine;

	transient private Class client;

	transient private Class clientRandom;

	private Map graphFactories;

	private GraphElement graphElement;

	private KeyHandler sharedKeyHandler;

	private PaletteRoot root;

	private PropertySheetPage undoablePropertySheetPage;

	private OverviewOutlinePage overviewOutlinePage;

	private List commandStackActionIDs;

	private List editorActionIDs;

	private boolean isDirty = false;

	private boolean editorSaving = false;

	private GraphEditPartFactory editPartFactory;

	private ResourceTracker resourceListener = new ResourceTracker();

	private IPartListener partListener = new IPartListener() {
		// If an open, unsaved file was deleted, query the user to either do a
		// "Save As"
		// or close the editor.
		public void partActivated(IWorkbenchPart part) {
			if (part != GraphEditor.this)
				return;
			if (!((FileEditorInput) getEditorInput()).getFile().exists()) {
				Shell shell = getSite().getShell();
				String title = IGraphEditorConstants.GraphicalEditor_FILE_DELETED_TITLE_UI;
				String message = IGraphEditorConstants.GraphicalEditor_FILE_DELETED_WITHOUT_SAVE_INFO;
				String[] buttons = {
						IGraphEditorConstants.GraphicalEditor_SAVE_BUTTON_UI,
						IGraphEditorConstants.GraphicalEditor_CLOSE_BUTTON_UI };
				MessageDialog dialog = new MessageDialog(shell, title, null,
						message, MessageDialog.QUESTION, buttons, 0);
				if (dialog.open() == 0) {
					if (!performSaveAs())
						partActivated(part);
				} else {
					closeEditor(false);
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	};

	private CommandStackListener commandStackListener = new CommandStackListener() {
		public void commandStackChanged(EventObject event) {
			updateActions(commandStackActionIDs);
			setDirty(getCommandStack().isDirty());
		}
	};

	private Controller controller;

	private StringBuffer bs_file;

	private String recFileNameForSaving;

	// This class listens to changes to the file system in the workspace, and
	// makes changes accordingly.
	// 1) An open, saved file gets deleted -> close the editor
	// 2) An open file gets renamed or moved -> change the editor's input
	// accordingly
	class ResourceTracker implements IResourceChangeListener,
			IResourceDeltaVisitor {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			} catch (CoreException exception) {
				// What should be done here?
			}
		}

		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| !delta.getResource().equals(
							((FileEditorInput) getEditorInput()).getFile()))
				return true;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				Display display = getSite().getShell().getDisplay();
				// if the file was deleted
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) {
					// NOTE: The case where an open, unsaved file is deleted is
					// being handled by the
					// PartListener added to the Workbench in the initialize()
					// method.
					display.asyncExec(new Runnable() {
						public void run() {
							if (!isDirty())
								closeEditor(false);
						}
					});
				} else { // else if it was moved or renamed
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getMovedToPath());
					display.asyncExec(new Runnable() {
						public void run() {
							superSetInput(new FileEditorInput(newFile));
						}
					});
				}
			} else if (delta.getKind() == IResourceDelta.CHANGED) {
				if (!editorSaving) {
					// the file was overwritten somehow (could have been
					// replaced by another
					// version in the respository)
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getFullPath());
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							setInput(new FileEditorInput(newFile));
							getCommandStack().flush();
						}
					});
				}
			}
			return false;
		}
	}

	/**
	 * Constructor
	 */
	public GraphEditor() {
		this.engine = new SimulatorEngine(this);
		this.commandStackActionIDs = new ArrayList();
		this.editorActionIDs = new ArrayList();
		this.graphFactories = new HashMap();
		this.createGraphFactories();
		this.setEditDomain(new DefaultEditDomain(this));
	}

	private CommandStack getCommanStack() {
		return this.getEditDomain().getCommandStack();
	}

	protected void setSite(IWorkbenchPartSite site) {
		// System.err.println("[GraphEditor] start setSite");
		super.setSite(site);

		// add listeners
		this.getCommandStack().addCommandStackListener(
				this.commandStackListener);

		this.getSite().getWorkbenchWindow().getPartService().addPartListener(
				this.partListener);

		// System.err.println("[GraphEditor] setSite successed");
	}

	/**
     * 
     */
	public void dispose() {

		// remove listeners
		this.getCommandStack().removeCommandStackListener(
				this.commandStackListener);

		this.getSite().getWorkbenchWindow().getPartService()
				.removePartListener(this.partListener);
		this.partListener = null;

		((FileEditorInput) getEditorInput()).getFile().getWorkspace()
				.removeResourceChangeListener(resourceListener);

		// dispose all actions
		getActionRegistry().dispose();

		// dispose the rest
		super.dispose();
	}

	protected void initializeGraphicalViewer() {
		// System.err.println("[GraphEditor] start initGraphViewer");
		super.initializeGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();

		viewer.setContents(this.getContent());

		this.getGraphElement().setShell(getEditorSite().getShell());
		// System.err.println("[GraphEditor] initGraphViewer successed "
		// + System.currentTimeMillis() + " : " + getPartName());
	}

	protected void configureGraphicalViewer() {
		// System.err.println("[GraphEditor] start config");
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();

		// config
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

		// register
		this.getEditDomain().addViewer(viewer);

		// activate
		this.getSite().setSelectionProvider(viewer);

		// initialize with input
		viewer.setEditPartFactory(this.getEditPartFactory());
		// If you don't put this line, then moving figures by drag & drop
		// above the left or top limit of the editor window will lead to
		// an infinite loop!
		((FigureCanvas) viewer.getControl())
				.setScrollBarVisibility(FigureCanvas.ALWAYS);

		// create menu
		ContextMenuProvider provider = new GraphContextMenuProvider(viewer,
				getActionRegistry());
		viewer.setContextMenu(provider);
		getSite().registerContextMenu(IGraphEditorConstants.CONTEXT_MENU,
				provider, viewer);

		// set keyboard handler
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer)
				.setParent(getCommonKeyHandler()));

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};

		// add listeners
		this.getGraphicalControl().addListener(SWT.Activate, listener);
		this.getGraphicalControl().addListener(SWT.Deactivate, listener);

		// System.err.println("[GraphEditor] config successed");

	}

	private Object getContent() {
		if (this.graphElement == null) {
			this.graphElement = new GraphElement();
		}
		return this.graphElement;
	}

	public GraphElement getGraphElement() {
		GraphElement e = (GraphElement) this.getContent();
		return e;
	}

	/**
	 * Get an execution engine corresponding to this editor
	 * 
	 * @return
	 */
	public SimulatorEngine getEngine() {
		return this.engine;
	}

	/**
	 * Get a client object that will be simulated by this editor's graph
	 * 
	 * @return
	 */
	public Class getClientObject() {
		return this.client;
	}

	public Class getClientRandomObject() {
		return this.clientRandom;
	}

	/**
	 * Set a client class object that will be simulated by this editor's graph
	 * 
	 * @param client
	 */
	public void setClientObject(Class client) {
		this.client = client;
	}

	public void setClientRandomObject(Class clientRandom) {
		this.clientRandom = clientRandom;
	}

	private void setContent(GraphElement element) {
		this.graphElement = element;
		this.setFactory(element);
	}

	private EditPartFactory getEditPartFactory() {
		if (this.editPartFactory == null)
			this.editPartFactory = new GraphEditPartFactory();

		return this.editPartFactory;
	}

	/**
     * 
     */
	public Object getAdapter(Class type) {

		if (type == GraphicalViewer.class || type == EditPartViewer.class)
			return this.getGraphicalViewer();
		else if (type == CommandStack.class)
			return this.getCommanStack();
		else if (type == EditDomain.class)
			return this.getEditDomain();
		else if (type == ActionRegistry.class)
			return this.getActionRegistry();
		else if (type == IPropertySheetPage.class)
			return this.getPropertySheetPage();
		else if (type == IContentOutlinePage.class)
			return this.getOverviewOutlinePage();

		return super.getAdapter(type);
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPalettePreferences()
	 */
	protected FlyoutPreferences getPalettePreferences() {
		return new FlyoutPreferences() {
			public int getDockLocation() {
				return GraphEditorPlugin.getDefault().getPreferenceStore()
						.getInt(PALETTE_DOCK_LOCATION);
			}

			public int getPaletteState() {
				return GraphEditorPlugin.getDefault().getPreferenceStore()
						.getInt(PALETTE_STATE);
			}

			public int getPaletteWidth() {
				return GraphEditorPlugin.getDefault().getPreferenceStore()
						.getInt(PALETTE_SIZE);
			}

			public void setDockLocation(int location) {
				GraphEditorPlugin.getDefault().getPreferenceStore().setValue(
						PALETTE_DOCK_LOCATION, location);
			}

			public void setPaletteState(int state) {
				GraphEditorPlugin.getDefault().getPreferenceStore().setValue(
						PALETTE_STATE, state);
			}

			public void setPaletteWidth(int width) {
				GraphEditorPlugin.getDefault().getPreferenceStore().setValue(
						PALETTE_SIZE, width);
			}
		};
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot()
	 */
	protected PaletteRoot getPaletteRoot() {
		if (root == null) {
			root = new PaletteRoot();
			root.addAll(this.createCategories(root));
		}
		return root;
	}

	/**
	 * Returns the KeyHandler with common bindings for both the Outline and
	 * Graphical Views. For example, delete is a common action.
	 */
	protected KeyHandler getCommonKeyHandler() {
		if (this.sharedKeyHandler == null) {
			this.sharedKeyHandler = new KeyHandler();
			this.sharedKeyHandler
					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
							getActionRegistry().getAction(
									ActionFactory.DELETE.getId()));
		}
		return this.sharedKeyHandler;
	}

	private void createGraphFactories() {
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_NODE,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_NODE));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_UNI_LINK,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_UNI_LINK));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_BI_LINK,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_BI_LINK));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_RING,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_RING));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_TREE,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_TREE));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_COMPLETE,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_COMPLETE));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_MESH,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_MESH));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_HYPER_CUBE,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_HYPER_CUBE));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_TORUS_1,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_TORUS_1));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_TORUS_2,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_TORUS_2));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_GENERIC,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_GENERIC));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_GENERIC_C,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_GENERIC_C));
	}

	private CreationFactory getFactory(String template) {
		return (CreationFactory) this.graphFactories.get(template);
	}

	private void setFactory(GraphElement element) {
		Iterator facts = this.graphFactories.keySet().iterator();
		for (String template = null; facts.hasNext();) {
			template = (String) facts.next();
			GraphElementFactory fac = (GraphElementFactory) this
					.getFactory(template);
			fac.setGraphElement(element);
		}
	}

	private List createCategories(PaletteRoot root) {
		List categories = new ArrayList();

		categories.add(createControlGroup(root));
		categories.add(createDrawerComponents());
		categories.add(createGraphComponents());
		return categories;
	}

	private PaletteContainer createControlGroup(PaletteRoot root) {

		PaletteGroup controlGroup = new PaletteGroup(
				IGraphEditorConstants.CONTROL_GROUP_LABEL);

		List entries = new ArrayList();
		ToolEntry tool = new PanningSelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);

		tool = new MarqueeToolEntry();
		entries.add(tool);

		PaletteSeparator sep = new PaletteSeparator(
				IGraphEditorConstants.SEPARATOR_ONE);
		sep
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(sep);
		controlGroup.addAll(entries);
		return controlGroup;
	}

	private PaletteContainer createDrawerComponents() {

		PaletteDrawer drawer = new PaletteDrawer(
				IGraphEditorConstants.DRAW_COMPONENTS, ImageDescriptor
						.createFromFile(GraphEditor.class, "icons/comp.gif"));//$NON-NLS-1$

		List entries = new ArrayList();

		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.DRAW_NODE,
				IGraphEditorConstants.DRAW_NODE_DESC,
				IGraphEditorConstants.TEMPLATE_NODE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_NODE),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/node.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/node.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		ToolEntry tool = new ConnectionCreationToolEntry(
				IGraphEditorConstants.DRAW_UNI_LINK,
				IGraphEditorConstants.DRAW_UNI_LINK_DESC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_UNI_LINK),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/uni_link.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/uni_link.gif")//$NON-NLS-1$
		);
		entries.add(tool);

		tool = new ConnectionCreationToolEntry(
				IGraphEditorConstants.DRAW_BI_LINK,
				IGraphEditorConstants.DRAW_BI_LINK_DESC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_BI_LINK),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/bi_link.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/bi_link.gif")//$NON-NLS-1$
		);
		entries.add(tool);

		drawer.addAll(entries);
		return drawer;
	}

	private PaletteContainer createGraphComponents() {

		PaletteDrawer drawer = new PaletteDrawer(
				IGraphEditorConstants.TOPOLOGY_TYPES,
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/topology.gif"));//$NON-NLS-1$

		List entries = new ArrayList();

		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.RING, IGraphEditorConstants.RING_DESC,
				IGraphEditorConstants.TEMPLATE_RING, this
						.getFactory(IGraphEditorConstants.TEMPLATE_RING),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TREE, IGraphEditorConstants.TREE_DESC,
				IGraphEditorConstants.TEMPLATE_TREE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TREE),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.COMPLETE,
				IGraphEditorConstants.COMPLETE_DESC,
				IGraphEditorConstants.TEMPLATE_COMPLETE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_COMPLETE),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.MESH, IGraphEditorConstants.MESH_DESC,
				IGraphEditorConstants.TEMPLATE_MESH, this
						.getFactory(IGraphEditorConstants.TEMPLATE_MESH),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.HYPER_CUBE,
				IGraphEditorConstants.HYPER_CUBE_DESC,
				IGraphEditorConstants.TEMPLATE_HYPER_CUBE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_HYPER_CUBE),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		PaletteStack toruses = new PaletteStack(
				IGraphEditorConstants.TORUS_STACK,
				IGraphEditorConstants.TORUS_STACK_DESC, null);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TORUS_1,
				IGraphEditorConstants.TORUS_1_DESC,
				IGraphEditorConstants.TEMPLATE_TORUS_1, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TORUS_1),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		toruses.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TORUS_2,
				IGraphEditorConstants.TORUS_2_DESC,
				IGraphEditorConstants.TEMPLATE_TORUS_2, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TORUS_2),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		toruses.add(combined);
		entries.add(toruses);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.GENERIC,
				IGraphEditorConstants.GENERIC_DESC,
				IGraphEditorConstants.TEMPLATE_GENERIC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_GENERIC),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.GENERIC_C,
				IGraphEditorConstants.GENERIC_C_DESC,
				IGraphEditorConstants.TEMPLATE_GENERIC_C, this
						.getFactory(IGraphEditorConstants.TEMPLATE_GENERIC_C),
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif"), //$NON-NLS-1$
				ImageDescriptor.createFromFile(GraphEditor.class,
						"icons/temp.gif")//$NON-NLS-1$
		);
		entries.add(combined);

		drawer.addAll(entries);
		return drawer;
	}

	protected CustomPalettePage createPalettePage() {
		return new CustomPalettePage(getPaletteViewerProvider()) {
			public void init(IPageSite pageSite) {
				super.init(pageSite);
				IAction copy = getActionRegistry().getAction(
						ActionFactory.COPY.getId());
				pageSite.getActionBars().setGlobalActionHandler(
						ActionFactory.COPY.getId(), copy);
			}
		};
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			private IMenuListener menuListener;

			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer.setCustomizer(new GraphPaletteCustomizer());
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}

			protected void hookPaletteViewer(PaletteViewer viewer) {
				super.hookPaletteViewer(viewer);
				final CopyTemplateAction copy = (CopyTemplateAction) getActionRegistry()
						.getAction(ActionFactory.COPY.getId());
				viewer.addSelectionChangedListener(copy);
				if (menuListener == null)
					menuListener = new IMenuListener() {
						public void menuAboutToShow(IMenuManager manager) {
							manager.appendToGroup(
									GEFActionConstants.GROUP_COPY, copy);
						}
					};
				viewer.getContextMenu().addMenuListener(menuListener);
			}
		};
	}

	protected void addStackAction(StackAction action) {
		getActionRegistry().registerAction(action);
		this.commandStackActionIDs.add(action.getId());
	}

	protected void addEditorAction(EditorPartAction action) {
		getActionRegistry().registerAction(action);
		this.editorActionIDs.add(action.getId());
	}

	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
	 */
	protected void firePropertyChange(int propertyId) {
		super.firePropertyChange(propertyId);
		this.updateActions(editorActionIDs);
	}

	/**
	 * Creates actions and registers them to the ActionRegistry.
	 */
	protected void createActions() {
		super.createActions();

		Action action = new DirectEditAction((IWorkbenchPart) this);
		this.addAction(action);
		getSelectionActions().add(action.getId());

		this.addAction(new CopyTemplateAction(this));
		this.addAction(new ProcessActions(this, IGraphEditorConstants.LOAD_ID));
		this.addAction(new ProcessActions(this,
				IGraphEditorConstants.LOAD_RANDOM_ID));
		this
				.addAction(new ProcessActions(this,
						IGraphEditorConstants.RESUME_ID));
		this.addAction(new ProcessActions(this, IGraphEditorConstants.STOP_ID));
		this.addAction(new ProcessActions(this,
				IGraphEditorConstants.SUSPEND_ID));
		this.addAction(new ProcessActions(this, IGraphEditorConstants.NEXT_ID));
		this
				.addAction(new ProcessActions(this,
						IGraphEditorConstants.SPEED_ID));
		this.addAction(new ProcessActions(this,IGraphEditorConstants.LOAD_RECORD_ID));
		this.addAction(new ProcessActions(this,IGraphEditorConstants.SAVE_RECORD_ID));
		
		this.addAction(new StateSettingAction(this,
				IGraphEditorConstants.ADD_STATE_ID));
		this.addAction(new StateSettingAction(this,
				IGraphEditorConstants.REMOVE_STATE_ID));
	}

	private PropertySheetPage getPropertySheetPage() {
		if (null == undoablePropertySheetPage) {
			undoablePropertySheetPage = new PropertySheetPage();
			undoablePropertySheetPage
					.setRootEntry(new UndoablePropertySheetEntry(
							getCommandStack()));
		}
		return undoablePropertySheetPage;
	}

	private OverviewOutlinePage getOverviewOutlinePage() {
		if (null == overviewOutlinePage && null != getGraphicalViewer()) {
			overviewOutlinePage = new OverviewOutlinePage(getGraphicalViewer());
		}
		return overviewOutlinePage;
	}

	protected void closeEditor(boolean save) {
		this.getSite().getPage().closeEditor(GraphEditor.this, save);
	}

	/*
	 * TODO write graph object into stream
	 */
	private void createOutputStream(OutputStream os) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(os);
		out.writeObject(this.getContent());
		out.close();
	}

	/*
	 * TODO read a saved file from stream
	 */
	public void setInput(IEditorInput input) {
		this.superSetInput(input);

		IFile file = ((IFileEditorInput) input).getFile();
		try {
			InputStream is = file.getContents(false);
			ObjectInputStream ois = new ObjectInputStream(is);
			this.setContent((GraphElement) ois.readObject());
			ois.close();

			// System.err.println("SetInput " + System.currentTimeMillis() + " "
			// + graphElement.getGraph());

		} catch (Exception e) {
			// This is just an example. All exceptions caught here.
			// when create a file for a first time
			// e.printStackTrace();
			System.err.println("[GraphEditor].setInput() " + e);
		}

		if (!editorSaving) {
			if (getGraphicalViewer() != null) {
				getGraphicalViewer().setContents(this.getContent());
				// loadProperties();
			}
			if (overviewOutlinePage != null) {
				overviewOutlinePage.setContents(this.getContent());
			}
		}
	}

	private void superSetInput(IEditorInput input) {
		// The workspace never changes for an editor. So, removing and re-adding
		// the
		// resourceListener is not necessary. But it is being done here for the
		// sake
		// of proper implementation. Plus, the resourceListener needs to be
		// added
		// to the workspace the first time around.
		if (getEditorInput() != null) {
			IFile file = ((FileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() != null) {
			IFile file = ((FileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(file.getName());
			this.graphElement.setGraphId(getPartName());
		}
	}

	protected void handleActivationChanged(Event event) {
		IAction copy = null;
		if (event.type == SWT.Deactivate)
			copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
		if (getEditorSite().getActionBars().getGlobalActionHandler(
				ActionFactory.COPY.getId()) != copy) {
			getEditorSite().getActionBars().setGlobalActionHandler(
					ActionFactory.COPY.getId(), copy);
			getEditorSite().getActionBars().updateActionBars();
		}
	}

	/**
     * 
     */
	public boolean isSaveOnCloseNeeded() {
		return getCommandStack().isDirty();
	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		try {
			editorSaving = true;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			this.createOutputStream(out);
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, monitor);
			out.close();
			this.getCommandStack().markSaveLocation();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			editorSaving = false;
		}
	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		this.performSaveAs();
		this.getCommandStack().markSaveLocation();
	}

	private boolean performSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
				.getShell());
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		IPath path = dialog.getResult();

		if (path == null)
			return false;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile file = workspace.getRoot().getFile(path);

		if (!file.exists()) {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				public void execute(final IProgressMonitor monitor) {
					try {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						createOutputStream(out);
						file.create(
								new ByteArrayInputStream(out.toByteArray()),
								true, monitor);
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			try {
				new ProgressMonitorDialog(getSite().getWorkbenchWindow()
						.getShell()).run(false, true, op);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			superSetInput(new FileEditorInput(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return isDirty;
	}

	private void setDirty(boolean dirty) {
		if (this.isDirty != dirty) {
			this.isDirty = dirty;
			this.firePropertyChange(EditorPart.PROP_DIRTY);
		}
	}

	public void makeDirty() {
		this.setDirty(true);
	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	public void setController(Controller controller) {
		this.controller=controller;
		
	}

	public Controller getController() {
		return controller;
	}

	public boolean setRecFile(String fileName) {
		boolean returnValue=true;
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
			bs_file= new StringBuffer();
			String k ;
			while (sc.hasNextLine()){
				k=sc.nextLine();
				bs_file.append(k);
				bs_file.append(System.getProperty("line.separator"));		
			}
			sc.close();			
			returnValue=true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returnValue=false;
		}
		return returnValue;		
	}

	public StringBuffer getRecFile() {
		return bs_file;
	}

	public void setRecFileNameForSaving(String fileName) {
		this.recFileNameForSaving=fileName;
		
	}

	public String getRecFileNameForSaving() {
		return recFileNameForSaving;
	}

}
