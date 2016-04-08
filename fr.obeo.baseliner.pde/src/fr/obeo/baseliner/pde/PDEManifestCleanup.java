package fr.obeo.baseliner.pde;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.pde.internal.ui.wizards.tools.OrganizeManifestsProcessor;
import org.osgi.service.component.annotations.Component;

import fr.obeo.baseliner.ManifestCleanup;

@Component
public class PDEManifestCleanup implements ManifestCleanup {

	private void clean(Collection<IProject> projects, IProgressMonitor pm) {
//		OrganizeManifestsProcessor organizer = new OrganizeManifestsProcessor(
//				new ArrayList(projects));
//		organizer.setCalculateUses(true);
//		organizer.setMarkInternal(true);
//		organizer.setPackageFilter("*.internal*");
//		organizer.setAddMissing(true);
//		
//		try {
//			Change change = organizer.createChange(pm);
//			change.initializeValidationData(pm);
//			if (change.isEnabled() && change.isValid(pm).isOK()) {
//				change.perform(pm);
//			}
//		} catch (OperationCanceledException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	@Override
	public void cleanup(File manifestFile) {
//		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
//				.findFilesForLocationURI(manifestFile.toURI());
//		Collection<IProject> projects = new LinkedHashSet<IProject>();
//		for (IFile iFile : files) {
//			if (iFile.exists() && iFile.getProject() != null) {
//				projects.add(iFile.getProject());
//			}
//		}
//		// TODO project monitor;
//		clean(projects, new NullProgressMonitor());

	}

}
