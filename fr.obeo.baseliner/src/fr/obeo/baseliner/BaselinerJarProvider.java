package fr.obeo.baseliner;

import java.io.File;

public interface BaselinerJarProvider {

	File getPreviousJar(String symbolicName, String bundleVersion);

}
