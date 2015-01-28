package fr.obeo.baseliner.ui.builder;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.google.common.io.Closeables;

import fr.obeo.baseliner.MEMApiChangeLog;

public class BaselineProjects extends AbstractHandler {

	/**
	 * Launche the baselining on the project.
	 * 
	 * @param projects
	 *            to baseline.
	 * @param monitor
	 * @throws CoreException
	 * @throws JavaModelException
	 * @throws FileNotFoundException
	 */
	private void baseline(List<IProject> projects, IProgressMonitor monitor) throws FileNotFoundException,
			JavaModelException, CoreException {
		Baseliner baseliner = new Baseliner();

		for (IProject iProject : projects) {
			MEMApiChangeLog changePerProject = new MEMApiChangeLog();
			baseliner.setChangeLog(changePerProject);
			baseliner.doBaseline(monitor, iProject);
			IFile changeFile = iProject.getFile("api_changes.textile");
			// FIXME encoding ! ! Don't use getBytes
			InputStream is = null;
			try {
				String report = changePerProject.report();
				if (report.length() > 0) {
					is = new ByteArrayInputStream(report.getBytes());
					if (changeFile.exists()) {
						changeFile.setContents(is, true, true, monitor);
					} else {
						changeFile.create(is, true, monitor);
					}
				}
			} finally {
				Closeables.closeQuietly(is);
			}
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final List<IProject> projects = new ArrayList<IProject>();
			for (Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				if (project != null) {
					projects.add(project);
				}
			}
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) throws CoreException {
					try {
						baseline(projects, monitor);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			};

			// Use the progess service to execute the runnable
			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			try {
				service.run(true, true, op);
			} catch (InvocationTargetException e) {
				// Operation was canceled
			} catch (InterruptedException e) {
				// Handle the wrapped exception
			}

		}
		return null;
	}

}
