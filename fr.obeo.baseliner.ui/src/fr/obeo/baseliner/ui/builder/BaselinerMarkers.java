package fr.obeo.baseliner.ui.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class BaselinerMarkers {
	private static final String MARKER_TYPE = "fr.obeo.baseliner.marker";

	public static void addMarker(IProject prj, IStatus stat) throws CoreException {
		IMarker marker = prj.createMarker(MARKER_TYPE);
		if (stat.getSeverity() == IStatus.ERROR) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} else if (stat.getSeverity() == IStatus.WARNING) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		} else if (stat.getSeverity() == IStatus.INFO) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		}
		marker.setAttribute(IMarker.MESSAGE, stat.getMessage());
	}

	public static void clearMarkers(IProject prj) throws CoreException {
		prj.deleteMarkers(MARKER_TYPE, true, 1);
	}

}
