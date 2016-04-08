package fr.obeo.baseliner.ui.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import fr.obeo.baseliner.ui.WorkspaceBuilder;
import fr.obeo.baseliner.ui.builder.BaselinerBuilderListener;

public class DisableOsgiSemanticVersioningBuilder extends Action
		implements IWorkbenchWindowActionDelegate, IActionDelegate2, BaselinerBuilderListener {

	public static final String ACTION_ID = "fr.obeo.baseline.ui.commands.DisableBuilderAction"; //$NON-NLS-1$

	// The real action if this is an action delegate
	private IAction fAction;

	/**
	 * Workbench part or <code>null</code> if not installed in a part
	 */
	private IWorkbenchPart fPart = null;

	public DisableOsgiSemanticVersioningBuilder() {
		super("Disable OSGi Semantic Versioning", AS_CHECK_BOX);
		setToolTipText("Disable OSGi Semantic Versioning");
		setDescription("Disable OSGi Semantic Versioning");
	}

	/**
	 * Constructs an action in the given part.
	 * 
	 * @param part
	 *            the part this action is created for
	 */
	public DisableOsgiSemanticVersioningBuilder(IWorkbenchPart part) {
		this();
		fPart = part;
		setId(ACTION_ID); // set action ID when created programmatically.
		updateActionCheckedState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IWorkbenchSiteProgressService progressService = null;
		if (fPart != null) {
			progressService = (IWorkbenchSiteProgressService) fPart.getSite()
					.getAdapter(IWorkbenchSiteProgressService.class);
		}
		final boolean enabled = !getWorkspaceBuilderManager().isEnabled();
		Job job = new Job(getText()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					WorkspaceBuilder bm = getWorkspaceBuilderManager();
					bm.setEnablement(enabled);
				}
				return Status.OK_STATUS;
			}
		};
		if (progressService != null) {
			progressService.schedule(job);
		} else {
			job.schedule();
		}
	}

	/**
	 * Updates the action's checked state to be opposite the enabled state of
	 * the breakpoint manager.
	 */
	public void updateActionCheckedState() {
		if (fAction != null) {
			fAction.setChecked(!getWorkspaceBuilderManager().isEnabled());
		} else {
			setChecked(!getWorkspaceBuilderManager().isEnabled());
		}
	}

	/**
	 * Returns the global breakpoint manager.
	 * 
	 * @return the global breakpoint manager
	 */
	public static WorkspaceBuilder getWorkspaceBuilderManager() {
		return WorkspaceBuilder.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		getWorkspaceBuilderManager().removeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		updateActionCheckedState();
		getWorkspaceBuilderManager().addListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		setChecked(action.isChecked());
		run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.
	 * IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fAction = action;
	}

	@Override
	public void enablementChanged(boolean enabled) {
		if (fAction != null) {
			fAction.setChecked(!enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		fAction = action;
		updateActionCheckedState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.
	 * IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
}
