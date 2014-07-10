package fr.obeo.baseliner.bnd;

import java.io.File;
import java.io.IOException;

import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;

public class WorkspaceJar extends Jar {

	public WorkspaceJar(File f) throws IOException {
		super(f);
	}

	@Override
	public boolean putResource(String path, Resource resource, boolean override) {
		/*
		 * we override the resource path given to "simulate" an actual jar. TODO
		 * : use the build.properties to do that reliably.
		 */
		if (path.startsWith("bin/")) {
			path = path.substring(4);
		}
		return super.putResource(path, resource, override);
	}

}
