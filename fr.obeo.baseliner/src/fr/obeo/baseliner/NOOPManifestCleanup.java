package fr.obeo.baseliner;

import java.io.File;

public class NOOPManifestCleanup implements ManifestCleanup {

	@Override
	public void cleanup(File manifestFile) {
		/*
		 * No-Op means we do nothing.
		 */
	}

}
