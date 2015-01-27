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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

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
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
						List<IProject> projects = new ArrayList<IProject>();
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
						try {
							baseline(projects, arg0);
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
