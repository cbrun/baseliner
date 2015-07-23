package fr.obeo.baseliner;

import java.io.File;
import java.util.Map;

public interface ApiComparator {
	public void loadNewClassesFromFolder(File projectFolder, File binFolder)
			throws RuntimeException;

	public void loadOldClassesFromFolder(File folder) throws RuntimeException;

	public Map<String, Delta> diffByPackage();
	
	public String getOldVersion();
}
