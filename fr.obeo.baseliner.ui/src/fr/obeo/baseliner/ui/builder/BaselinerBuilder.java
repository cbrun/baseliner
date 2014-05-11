package fr.obeo.baseliner.ui.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fr.obeo.baseliner.PluginBaseliner;
import fr.obeo.baseliner.ui.BaselinerUIPlugin;

public class BaselinerBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "fr.obeo.baseliner.ui.BaslinerBuilder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		// if (kind == FULL_BUILD) {
		fullBuild(monitor);
		// } else {
		// IResourceDelta delta = getDelta(getProject());
		// if (delta == null) {
		// fullBuild(monitor);
		// } else {
		// incrementalBuild(delta, monitor);
		// }
		// }
		return null;
	}

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

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			IFile manifestFile = getProject().getFile("META-INF/MANIFEST.MF");
			if (manifestFile.exists() && manifestFile.isAccessible()) {
				PluginBaseliner baseliner = BaselinerUIPlugin.getDefault().baseliner;
				if (hasNature(BaselinerConstants.JAVA_NATURE_ID)
						&& hasNature(BaselinerConstants.PLUGIN_NATURE_ID)
						&& baseliner != null) {
					IJavaProject javaProject = JavaCore.create(getProject());
					baseliner.updateManifestFile(new File(manifestFile
							.getLocation().toOSString()),
							declareJavaOutputFolders(baseliner, javaProject),
							Collections.EMPTY_LIST);
					manifestFile.refreshLocal(1, monitor);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			// TODO log
		} catch (FileNotFoundException e) {
			// TODO log
			e.printStackTrace();
		}
	}

	private Collection<File> declareJavaOutputFolders(
			PluginBaseliner baseliner, IJavaProject javaProject)
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

				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(inputFolderPath.lastSegment());
				if (!(project != null && project.exists() && project
						.equals(javaProject.getProject()))) {
					IContainer inputContainer = ResourcesPlugin.getWorkspace()
							.getRoot().getFolder(inputFolderPath);
					IContainer outputContainer = ResourcesPlugin.getWorkspace()
							.getRoot().getFolder(outputFolderPath);

					if (inputContainer != null && outputContainer != null) {
						File inputDirectory = inputContainer.getLocation()
								.toFile();
						File outputDirectory = outputContainer.getLocation()
								.toFile();
						outputDirs.add(outputDirectory);
						// baseliner.addJavaSourceDirectory(inputDirectory);
					}
				}
			}
		}
		return outputDirs;
	}

	private boolean hasNature(String natureToLookFor) throws CoreException {
		for (String natures : getProject().getDescription().getNatureIds()) {
			if (natures.equals(natureToLookFor)) {
				return true;
			}
		}
		return false;
	}

	// protected void incrementalBuild(IResourceDelta delta,
	// IProgressMonitor monitor) throws CoreException {
	// // the visitor does the work.
	// delta.accept(new SampleDeltaVisitor(new XMLChecker()));
	// }
}
