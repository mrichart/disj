package distributed.plugin.ui.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.views.ViewsPlugin;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * Main class for the DisJ View.
 * <p>
 * This standard view has id <code>"distributed.plugin.ui.view.DisJViewer"</code>.
 * </p>
 * When a <b>DisJ view</b> notices an editor being activated, it 
 * asks the editor whether it has a <b>disJ page</b> to include
 * in the disJ view. This is done using <code>getAdapter</code>:
 * <pre>
 * IEditorPart editor = ...;
 * IDisJViewable view = (IDisJViewable) editor.getAdapter(IDisJViewable.class);
 * if (view != null) {
 *    // editor wishes to contribute outlinePage to disJ view
 * }
 * </pre>
 * If the editor supports a content outline page, the editor instantiates
 * and configures the page, and returns it. This page is then added to the 
 * disJ view (a pagebook which presents one page at a time) and 
 * immediately made the current page (the disJ view need not be
 * visible). If the editor does not support a disJ page, the disJ
 * view shows a special default page which makes it clear to the user
 * that the disJ view is disengaged. A disJ page is free
 * to report selection events; the disJ view forwards these events 
 * along to interested parties. When the disJ view notices a
 * different editor being activated, it flips to the editor's corresponding
 * disJ page. When the disJ view notices an editor being
 * closed, it destroys the editor's corresponding disJ page.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a 
 * DisJ view is needed for a workbench window. This class was not intended
 * to be instantiated or subclassed by clients.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DisJViewer extends PageBookView implements ISelectionProvider,
		ISelectionChangedListener {

    /**
     * The plugin prefix.
     */
   // public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

    /**
     * Help context id used for the content outline view
     * (value <code>"org.eclipse.ui.content_outline_context"</code>).
     */
   // public static final String CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID = PREFIX
   //         + "content_outline_context";//$NON-NLS-1$

    /**
     * Message to show on the default page.
     */
    private String defaultText = "View is not available."; 

    /**
     * Creates a DisJ view with no viewer pages.
     */
    public DisJViewer() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        getSelectionProvider().addSelectionChangedListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        //page.setMessage(defaultText);
        return page;
    }

    /**
     * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(),
        //         CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected PageRec doCreatePage(IWorkbenchPart part) {
        // Try to get a disJ page.
        Object obj = ViewsPlugin.getAdapter(part, IDisJViewable.class, false);
        if (obj instanceof IDisJViewable) {
            IDisJViewable page = (IDisJViewable) obj;
            if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
            page.createControl(getPageBook());
            return new PageRec(part, page);
        }
        // There is no disJ view page
        return null;
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IDisJViewable page = (IDisJViewable) rec.page;
        page.dispose();
        rec.dispose();
    }

    /* (non-Javadoc)
     * Method declared on IAdaptable.
     */
    public Object getAdapter(Class key) {
        if (key == IContributedContentsView.class) {
			return new IContributedContentsView() {
                public IWorkbenchPart getContributingPart() {
                    return getContributingEditor();
                }
            };
		}
        return super.getAdapter(key);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
			return page.getActiveEditor();
		}

        return null;
    }

    /**
     * Returns the editor which contributed the current 
     * page to this view.
     *
     * @return the editor which contributed the current page
     * or <code>null</code> if no editor contributed the current page
     */
    private IWorkbenchPart getContributingEditor() {
        return getCurrentContributingPart();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public ISelection getSelection() {
        // get the selection from the selection provider
        return getSelectionProvider().getSelection();
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * We only want to track editors.
     */
    protected boolean isImportant(IWorkbenchPart part) {
        //We only care about editors
        return (part instanceof IEditorPart);
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     * Treat this the same as part activation.
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        partActivated(part);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        getSelectionProvider().removeSelectionChangedListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionChangedListener.
     */
    public void selectionChanged(SelectionChangedEvent event) {
        getSelectionProvider().selectionChanged(event);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void setSelection(ISelection selection) {
        getSelectionProvider().setSelection(selection);
    }

    /**
     * The <code>DisJViewer</code> implementation of this <code>PageBookView</code> method
     * extends the behavior of its parent to use the current page as a selection provider.
     * 
     * @param pageRec the page record containing the page to show
     */
    protected void showPageRec(PageRec pageRec) {
        IPageSite pageSite = getPageSite(pageRec.page);
        ISelectionProvider provider = pageSite.getSelectionProvider();
        if (provider == null && (pageRec.page instanceof IDisJViewable)) {
			// This means that the page did not set a provider during its initialization 
            // so for backward compatibility we will set the page itself as the provider.
            pageSite.setSelectionProvider((IDisJViewable) pageRec.page);
		}
        super.showPageRec(pageRec);
    }
}
