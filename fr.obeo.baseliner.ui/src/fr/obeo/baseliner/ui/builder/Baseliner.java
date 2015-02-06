package fr.obeo.baseliner.ui.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Optional;
import com.google.common.io.Closeables;

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
		BaselinerMarkers.clearMarkers(project);
		if (manifestFile.exists() && manifestFile.isAccessible()) {
			PluginBaseliner baseliner = BaselinerUIPlugin.getDefault().baseliner;
			if (hasNature(project, BaselinerConstants.JAVA_NATURE_ID)
					&& hasNature(project, BaselinerConstants.PLUGIN_NATURE_ID) && baseliner != null) {
				IJavaProject javaProject = JavaCore.create(project);

				if (!monitor.isCanceled()) {
					baseliner.setJarProviderSource(BaselinerPreferences.getJarProviderSource(project));
					ManifestChanges change = baseliner.updateManifestFile(new File(manifestFile.getLocation()
							.toOSString()), declareJavaOutputFolders(baseliner, javaProject), Collections.EMPTY_LIST,
							monitor);
					if (change.getUpdatedManifestContent().isPresent()) {
						updatedContent = change.getUpdatedManifestContent().get();
					}
					if (changeLog != null) {
						changeLog.aggregate(project.getName(), change.getPackageChanges());
					}
					for (IStatus infos : change.getStatuses()) {
						BaselinerMarkers.addMarker(project, infos);
					}
					if (change.bundleVersionUpdate()) {
						manifestFile.refreshLocal(1, monitor);
						project.refreshLocal(1, monitor);
					} else {
						manifestFile.refreshLocal(1, monitor);
					}
				}

			}
		}

		return Optional.fromNullable(updatedContent);
	}

	public void updateAPIReport(final IProgressMonitor monitor, IProject project) throws CoreException {
		IFile changeFile = project.getFile("api_changes.textile");
		// FIXME encoding ! ! Don't use getBytes
		InputStream is = null;
		try {
			String report = changeLog.report();
			is = new ByteArrayInputStream(report.getBytes());
			if (changeFile.exists()) {
				changeFile.setContents(is, true, true, monitor);
			} else if (report != null && report.trim().length() > 0) {
				changeFile.create(is, true, monitor);
			}
			changeFile.setDerived(true, monitor);
		} finally {
			Closeables.closeQuietly(is);
		}
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
