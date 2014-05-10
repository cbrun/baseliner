package fr.obeo.baseliner;

import java.io.File;
import java.util.Map;

import org.osgi.framework.Version;

public interface ApiComparator {
	public void loadNewClassesFromFolder(File folder) throws RuntimeException;

	public void loadOldClassesFromFolder(File folder) throws RuntimeException;

	public Version getOldPackageVersion(String ns);

	public Map<String, Delta> diffByPackage();
}
