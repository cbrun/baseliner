package fr.obeo.baseliner;

import org.osgi.framework.Version;

public interface Delta {

	Version infer(Version oldPackageVersion);

}
