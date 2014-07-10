package fr.obeo.baseliner.ui.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleNatureHandler extends AbstractHandler {

	/**
	 * Toggles baseliner nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			BaselinerNature nature = new BaselinerNature();
			nature.setProject(project);
			String[] natures = description.getNatureIds();
			if (description.hasNature(BaselinerNature.NATURE_ID)) {
				nature.deconfigure();
				for (int i = 0; i < natures.length; ++i) {
					if (BaselinerNature.NATURE_ID.equals(natures[i])) {
						// Remove the nature
						String[] newNatures = new String[natures.length - 1];
						System.arraycopy(natures, 0, newNatures, 0, i);
						System.arraycopy(natures, i + 1, newNatures, i,
								natures.length - i - 1);
						description.setNatureIds(newNatures);
						project.setDescription(description,
								new NullProgressMonitor());

						break;
					}
				}

			} else {
				// Add the nature
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 1, natures.length);
				newNatures[0] = BaselinerNature.NATURE_ID;
				description.setNatureIds(newNatures);
				project.setDescription(description,
						new NullProgressMonitor());
				nature.configure();
			}

		} catch (CoreException e) {
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			try {
				PlatformUI.getWorkbench().getProgressService()
						.run(true, false, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor arg0)
									throws InvocationTargetException,
									InterruptedException {
								for (Iterator it = ((IStructuredSelection) selection)
										.iterator(); it.hasNext();) {
									Object element = it.next();
									IProject project = null;
									if (element instanceof IProject) {
										project = (IProject) element;
									} else if (element instanceof IAdaptable) {
										project = (IProject) ((IAdaptable) element)
												.getAdapter(IProject.class);
									}
									if (project != null) {
										toggleNature(project);
									}
								}
							}

						});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
