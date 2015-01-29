package fr.obeo.baseliner;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface BaselinerJarProvider {

	File getPreviousJar(String catalogURI, String symbolicName, String bundleVersion, IProgressMonitor monitor,Collection<IStatus> statuses);

}
