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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.gef.EditPartViewer;
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
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
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
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import distributed.plugin.core.DisJException;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.runtime.IDistributedModel;
import distributed.plugin.runtime.adversary.AbstractControl;
import distributed.plugin.runtime.engine.SimulatorEngine;
import distributed.plugin.ui.Activator;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.actions.ProcessActions;
import distributed.plugin.ui.actions.StateSettingAction;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.parts.GraphEditPartFactory;
import distributed.plugin.ui.view.IDisJViewable;
import distributed.plugin.ui.view.OverviewDisjPage;
import distributed.plugin.ui.view.OverviewOutlinePage;

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
		Activator.getDefault().getPreferenceStore().setDefault(
				PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
	}

	private SimulatorEngine engine;

	transient private Class<IDistributedModel> client;

	transient private Class<IRandom> clientRandom;

	transient private Class<AbstractControl> clientAdver;
	
	transient private ClassLoader loader;

	private Map<String, GraphElementFactory> graphFactories;

	private GraphElement graphElement;

	private KeyHandler sharedKeyHandler;

	private PaletteRoot root;

	private PropertySheetPage undoablePropertySheetPage;

	private OverviewOutlinePage overviewOutlinePage;
	
	private OverviewDisjPage agentViewPage;

	private List<String> commandStackActionIDs;

	private List<String> editorActionIDs;

	private boolean isDirty = false;

	private boolean editorSaving = false;

	private GraphEditPartFactory editPartFactory;

	private ResourceTracker resourceListener = new ResourceTracker();
	
	
	private IPartListener partListener = new IPartListener() {
		// If an open, unsaved file was deleted, query the user to either do a
		// "Save As" or close the editor.
		public void partActivated(IWorkbenchPart part) {
			if (part != GraphEditor.this){
				return;
			}
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
		this.engine = new SimulatorEngine();
		this.commandStackActionIDs = new ArrayList<String>();
		this.editorActionIDs = new ArrayList<String>();
		this.graphFactories = new HashMap<String, GraphElementFactory>();
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
/*
		// this is for testing
		IWorkbenchPage[] ps = this.getSite().getWorkbenchWindow().getPages();
		//ps[0].
		
		//Any workbench part that holds a viewer should register this viewer as 
		// the selection provider with the respective view site: 
		getSite().setSelectionProvider(tableviewer);

		// You can retrieve the selection provider from a workbench part from its site. 
		IWorkbenchPartSite i_site = this.getSite();
		ISelectionProvider provider = i_site.getSelectionProvider();
		// this can be null if the workbench part hasn't set one, better safe than sorry
		if (provider != null) {
		    provider.setSelection();
		}
*/		
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

		viewer.setContents(this.getGraphElement());

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


	private void setContent(GraphElement element) {
		this.graphElement = element;
		try {
			// update tracker with latest version from a file
			// since every editor opening does create new element
			// with default values
			GraphFactory.addGraph(this.graphElement.getGraph());
		} catch (DisJException e) {}
		this.setFactory(element);
	}

	public GraphElement getGraphElement() {
		if(this.graphElement == null){
			this.graphElement = (GraphElement)this.getContent();
		}
		return this.graphElement;	
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
	public Class<IDistributedModel> getClientObject() {
		return this.client;
	}

	public Class<IRandom> getClientRandomObject() {
		return this.clientRandom;
	}

	public Class<AbstractControl> getClientAdversaryObject() {
		return this.clientAdver;
	}

	/**
	 * Set a client class object that will be simulated by this editor's graph
	 * 
	 * @param client
	 */
	public void setClientObject(Class<IDistributedModel> client) {
		this.client = client;
	}

	public void setClientRandomObject(Class<IRandom> clientRandom) {
		this.clientRandom = clientRandom;
	}

	public void setClientAdversaryObject(Class<AbstractControl> clientAdver) {
		this.clientAdver = clientAdver;
	}
	
	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
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
		else if (type == IDisJViewable.class)
			return this.getAgentPage();
		
		return super.getAdapter(type);
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPalettePreferences()
	 */
	protected FlyoutPreferences getPalettePreferences() {
		return new FlyoutPreferences() {
			public int getDockLocation() {
				return Activator.getDefault().getPreferenceStore()
						.getInt(PALETTE_DOCK_LOCATION);
			}

			public int getPaletteState() {
				return Activator.getDefault().getPreferenceStore()
						.getInt(PALETTE_STATE);
			}

			public int getPaletteWidth() {
				return Activator.getDefault().getPreferenceStore()
						.getInt(PALETTE_SIZE);
			}

			public void setDockLocation(int location) {
				Activator.getDefault().getPreferenceStore().setValue(
						PALETTE_DOCK_LOCATION, location);
			}

			public void setPaletteState(int state) {
				Activator.getDefault().getPreferenceStore().setValue(
						PALETTE_STATE, state);
			}

			public void setPaletteWidth(int width) {
				Activator.getDefault().getPreferenceStore().setValue(
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
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_CONNECTED,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_CONNECTED));
		this.graphFactories.put(IGraphEditorConstants.TEMPLATE_SPATIAL,
				new GraphElementFactory(this.getGraphElement(),
						IGraphEditorConstants.TEMPLATE_SPATIAL));
	}

	private CreationFactory getFactory(String template) {
		return (CreationFactory) this.graphFactories.get(template);
	}

	private void setFactory(GraphElement element) {
		Iterator<String> facts = this.graphFactories.keySet().iterator();
		for (String template = null; facts.hasNext();) {
			template = facts.next();
			GraphElementFactory fac = (GraphElementFactory) this
					.getFactory(template);
			fac.setGraphElement(element);
		}
	}

	private List<PaletteContainer> createCategories(PaletteRoot root) {
		List<PaletteContainer> categories = new ArrayList<PaletteContainer>();
		categories.add(this.createControlGroup(root));
		categories.add(this.createDrawerComponents());
		categories.add(this.createGraphComponents());
		return categories;
	}

	/*
	 * Selection option
	 */
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

	/*
	 * Create a raw graph manually
	 */
	private PaletteContainer createDrawerComponents() {

		URL installUrl = Activator.getDefault().getBundle().getEntry(
				"/");
		URL imageUrl = null;

		try {
			imageUrl = new URL(installUrl, "icons/draw.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		PaletteDrawer drawer = new PaletteDrawer(
				IGraphEditorConstants.DRAW_COMPONENTS, ImageDescriptor
						.createFromURL(imageUrl));

		List entries = new ArrayList();

		try {
			imageUrl = new URL(installUrl, "icons/node.gif");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.DRAW_NODE,
				IGraphEditorConstants.DRAW_NODE_DESC,
				IGraphEditorConstants.TEMPLATE_NODE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_NODE),
				ImageDescriptor.createFromURL(imageUrl), ImageDescriptor
						.createFromURL(imageUrl));
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/uni_link.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		ToolEntry tool = new ConnectionCreationToolEntry(
				IGraphEditorConstants.DRAW_UNI_LINK,
				IGraphEditorConstants.DRAW_UNI_LINK_DESC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_UNI_LINK),
				ImageDescriptor.createFromURL(imageUrl), ImageDescriptor
						.createFromURL(imageUrl));
		entries.add(tool);

		try {
			imageUrl = new URL(installUrl, "icons/bi_link.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		tool = new ConnectionCreationToolEntry(
				IGraphEditorConstants.DRAW_BI_LINK,
				IGraphEditorConstants.DRAW_BI_LINK_DESC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_BI_LINK),
				ImageDescriptor.createFromURL(imageUrl), ImageDescriptor
						.createFromURL(imageUrl));
		entries.add(tool);

		drawer.addAll(entries);
		return drawer;
	}

	/*
	 * Create a build in topology
	 */
	private PaletteContainer createGraphComponents() {
		
		URL installUrl = Activator.getDefault().getBundle().getEntry("/");
		URL imageUrl = null;
		
		try {
			imageUrl = new URL(installUrl, "icons/graph.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		PaletteDrawer drawer = new PaletteDrawer(
				IGraphEditorConstants.TOPOLOGY_TYPES,
				ImageDescriptor.createFromURL(imageUrl));

		List entries = new ArrayList();

		try {
			imageUrl = new URL(installUrl, "icons/ring.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.RING, IGraphEditorConstants.RING_DESC,
				IGraphEditorConstants.TEMPLATE_RING, this
						.getFactory(IGraphEditorConstants.TEMPLATE_RING),
						ImageDescriptor.createFromURL(imageUrl),
						ImageDescriptor.createFromURL(imageUrl)
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/tree.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TREE, IGraphEditorConstants.TREE_DESC,
				IGraphEditorConstants.TEMPLATE_TREE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TREE),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/complete.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}	
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.COMPLETE,
				IGraphEditorConstants.COMPLETE_DESC,
				IGraphEditorConstants.TEMPLATE_COMPLETE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_COMPLETE),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/spatial.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.SPATIAL,
				IGraphEditorConstants.SPATIAL_DESC,
				IGraphEditorConstants.TEMPLATE_SPATIAL, this
						.getFactory(IGraphEditorConstants.TEMPLATE_SPATIAL),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/mesh.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.MESH, IGraphEditorConstants.MESH_DESC,
				IGraphEditorConstants.TEMPLATE_MESH, this
						.getFactory(IGraphEditorConstants.TEMPLATE_MESH),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/hypercube.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.HYPER_CUBE,
				IGraphEditorConstants.HYPER_CUBE_DESC,
				IGraphEditorConstants.TEMPLATE_HYPER_CUBE, this
						.getFactory(IGraphEditorConstants.TEMPLATE_HYPER_CUBE),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		entries.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/torus.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		PaletteStack toruses = new PaletteStack(
				IGraphEditorConstants.TORUS_STACK,
				IGraphEditorConstants.TORUS_STACK_DESC, null);

		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TORUS_1,
				IGraphEditorConstants.TORUS_1_DESC,
				IGraphEditorConstants.TEMPLATE_TORUS_1, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TORUS_1),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		toruses.add(combined);

		try {
			imageUrl = new URL(installUrl, "icons/torus2.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.TORUS_2,
				IGraphEditorConstants.TORUS_2_DESC,
				IGraphEditorConstants.TEMPLATE_TORUS_2, this
						.getFactory(IGraphEditorConstants.TEMPLATE_TORUS_2),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		toruses.add(combined);
		entries.add(toruses);

		try {
			imageUrl = new URL(installUrl, "icons/conn.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		PaletteStack random = new PaletteStack(
				IGraphEditorConstants.RANDOM_STACK,
				IGraphEditorConstants.RANDOM_STACK_DESC, null);
		
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.CONNECTED,
				IGraphEditorConstants.CONNECTED_DESC,
				IGraphEditorConstants.TEMPLATE_CONNECTED, this
						.getFactory(IGraphEditorConstants.TEMPLATE_CONNECTED),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);		
		random.add(combined);
		
		try {
			imageUrl = new URL(installUrl, "icons/genn.png");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		combined = new CombinedTemplateCreationEntry(
				IGraphEditorConstants.FOREST,
				IGraphEditorConstants.FORREST_DESC,
				IGraphEditorConstants.TEMPLATE_GENERIC, this
						.getFactory(IGraphEditorConstants.TEMPLATE_GENERIC),
						ImageDescriptor.createFromURL(imageUrl), //$NON-NLS-1$
						ImageDescriptor.createFromURL(imageUrl)//$NON-NLS-1$
		);
		random.add(combined);
		entries.add(random);

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

		ProcessActions procAct;

		Action action = new DirectEditAction((IWorkbenchPart) this);
		this.addAction(action);
		getSelectionActions().add(action.getId());

		this.addAction(new CopyTemplateAction(this));

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_LOAD);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_LOAD_RANDOM);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_LOAD_ADVERSARY);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_RESUME);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_STOP);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_SUSPEND);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_STEP_NEXT);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_SET_SPEED);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_REPLAY_RECORD);
		this.addAction(procAct);

		procAct = new ProcessActions(this, IGraphEditorConstants.ACTION_SAVE_RECORD);
		this.addAction(procAct);

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

	private OverviewDisjPage getAgentPage() {
		if (agentViewPage == null) {
			agentViewPage = new OverviewDisjPage(this.getGraphElement());
			//GraphElement ge = this.getGraphElement();
			//ge.setViewListener(this.agentViewPage);
		}
		return agentViewPage;
	}
	
	protected void closeEditor(boolean save) {
		this.getSite().getPage().closeEditor(GraphEditor.this, save);
	}

	/*
	 *
	 */
	private void createOutputStream(OutputStream os) throws IOException {
		try {
			ObjectOutputStream out = new ObjectOutputStream(os);
			this.writeObject(out);
		} catch (IOException e) {
			System.err.println("[GraphEditor].createOutputStream() " + e);
			throw e;
		}

	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		Object obj = this.getGraphElement();
		oos.writeObject(obj);
		oos.close();
	}

	/*
	 * Read a saved file from stream
	 */
	public void setInput(IEditorInput input) {

		this.superSetInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		ObjectInputStream ois = null;
		InputStream is = null;
		try {
			is = file.getContents(false);
			ois = new ObjectInputStream(is);
			this.readObject(ois);

		} catch (Exception e) {
			// EOFException
			// Happens when create a file for a first time!!!!
			// e.printStackTrace();
			System.out.println("[GraphEditor].setInput() File "
					+ file.getName() + " is created");
		} finally {
			if (ois != null) {
				try {
					ois.close();
					ois = null;
				} catch (IOException e) {
				}
			}
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (IOException e) {
				}
			}
		}

		if (!editorSaving) {
			if (getGraphicalViewer() != null) {
				getGraphicalViewer().setContents(this.getGraphElement());
				// loadProperties();
			}
			if (overviewOutlinePage != null) {
				overviewOutlinePage.setContents(this.getGraphElement());
			}
			if(agentViewPage != null){
				agentViewPage.setContents(this.getGraphElement());
			}
		}
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		GraphElement g = (GraphElement) ois.readObject();
		this.setContent(g);
	}

	private void superSetInput(IEditorInput input) {
		try {
			// The workspace never changes for an editor. So, removing and
			// re-adding
			// the
			// resourceListener is not necessary. But it is being done here for
			// the
			// sake
			// of proper implementation. Plus, the resourceListener needs to be
			// added
			// to the workspace the first time around.
			if (getEditorInput() != null) {
				IFile file = ((FileEditorInput) getEditorInput()).getFile();
				file.getWorkspace().removeResourceChangeListener(
						resourceListener);
			}

			super.setInput(input);

			if (getEditorInput() != null) {
				IFile file = ((FileEditorInput) getEditorInput()).getFile();
				file.getWorkspace().addResourceChangeListener(resourceListener);
				setPartName(file.getName());
				this.graphElement.setGraphId(getPartName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[GraphEditor].superSetInput() " + e);
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
		ByteArrayOutputStream out = null;
		try {
			editorSaving = true;
			out = new ByteArrayOutputStream();
			this.createOutputStream(out);
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			byte[] contents = out.toByteArray();
			file.setContents(new ByteArrayInputStream(contents, 0,
					contents.length), true, false, monitor);
			this.getCommandStack().markSaveLocation();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[GraphEditor].doSave() " + e);
		} finally {
			editorSaving = false;
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
				out = null;
			}
		}
	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		this.performSaveAs();
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
					ByteArrayOutputStream out = null;
					try {
						out = new ByteArrayOutputStream();
						createOutputStream(out);
						byte[] contents = out.toByteArray();
						file.create(new ByteArrayInputStream(contents, 0,
								contents.length), true, monitor);
					} catch (Exception e) {
						e.printStackTrace();
						System.err
								.println("[GraphEditor].performSaveAs().execute() "
										+ e);
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
							}
							out = null;
						}
					}
				}
			};
			try {
				new ProgressMonitorDialog(getSite().getWorkbenchWindow()
						.getShell()).run(false, true, op);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("[GraphEditor].performSaveAs() " + e);
			}
		}

		try {
			this.superSetInput(new FileEditorInput(file));
			this.getCommandStack().markSaveLocation();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[GraphEditor].performSaveAs() " + e);
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


}
