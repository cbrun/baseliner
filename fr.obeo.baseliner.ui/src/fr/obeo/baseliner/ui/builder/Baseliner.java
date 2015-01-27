package fr.obeo.baseliner.ui.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Optional;

import fr.obeo.baseliner.ApiChangeLog;
import fr.obeo.baseliner.MEMApiChangeLog;
import fr.obeo.baseliner.ManifestChanges;
import fr.obeo.baseliner.PluginBaseliner;
import fr.obeo.baseliner.ui.BaselinerUIPlugin;

public class Baseliner {

	/**
	 * The Java nature.
	 * 
	 * @since 3.1
	 */
	String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$

	/**
	 * The plugin nature.
	 * 
	 * @since 3.1
	 */
	String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$

	public ApiChangeLog changeLog = new MEMApiChangeLog();

	public void setChangeLog(ApiChangeLog changeLog) {
		this.changeLog = changeLog;
	}

	public Baseliner() {
		super();
	}

	public Optional<String> doBaseline(final IProgressMonitor monitor, IProject project) throws CoreException,
			FileNotFoundException, JavaModelException {
		String updatedContent = null;
		IFile manifestFile = project.getFile("META-INF/MANIFEST.MF");
		if (manifestFile.exists() && manifestFile.isAccessible()) {
			PluginBaseliner baseliner = BaselinerUIPlugin.getDefault().baseliner;
			if (hasNature(project, BaselinerConstants.JAVA_NATURE_ID)
					&& hasNature(project, BaselinerConstants.PLUGIN_NATURE_ID) && baseliner != null) {
				IJavaProject javaProject = JavaCore.create(project);
				ManifestChanges change = baseliner.updateManifestFile(
						new File(manifestFile.getLocation().toOSString()),
						declareJavaOutputFolders(baseliner, javaProject), Collections.EMPTY_LIST);
				if (change.getFileContent().isPresent()) {
					updatedContent = change.getFileContent().get();
				}
				manifestFile.refreshLocal(1, monitor);
				if (changeLog != null) {
					changeLog.aggregate(project.getName(), change.getChanges());
				}

			}
		}
		return Optional.fromNullable(updatedContent);
	}

	private boolean hasNature(IProject project, String natureToLookFor) throws CoreException {
		for (String natures : project.getDescription().getNatureIds()) {
			if (natures.equals(natureToLookFor)) {
				return true;
			}
		}
		return false;

	}

	private Collection<File> declareJavaOutputFolders(PluginBaseliner baseliner, IJavaProject javaProject)
			throws JavaModelException {
		List<File> outputDirs = new ArrayList<File>();
		for (IClasspathEntry iClasspathEntry : javaProject.getRawClasspath()) {
			int entryKind = iClasspathEntry.getEntryKind();
			if (IClasspathEntry.CPE_SOURCE == entryKind) {
				// We have the source folders of the project.
				IPath inputFolderPath = iClasspathEntry.getPath();
				IPath outputFolderPath = iClasspathEntry.getOutputLocation();

				if (outputFolderPath == null) {
					outputFolderPath = javaProject.getOutputLocation();
				}

				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(inputFolderPath.lastSegment());
				if (!(project != null && project.exists() && project.equals(javaProject.getProject()))) {
					IContainer inputContainer = ResourcesPlugin.getWorkspace().getRoot().getFolder(inputFolderPath);
					IContainer outputContainer = ResourcesPlugin.getWorkspace().getRoot().getFolder(outputFolderPath);

					if (inputContainer != null && outputContainer != null) {
						File inputDirectory = inputContainer.getLocation().toFile();
						File outputDirectory = outputContainer.getLocation().toFile();
						outputDirs.add(outputDirectory);
						// baseliner.addJavaSourceDirectory(inputDirectory);
					}
				}
			}
		}
		return outputDirs;
	}
}
