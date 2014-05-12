package fr.obeo.baseliner.bnd;

import org.osgi.framework.Version;

import aQute.bnd.service.diff.Diff;
import fr.obeo.baseliner.Delta;

public class BndDelta implements Delta {

	private Diff packageDiff;
	private aQute.bnd.version.Version version;

	public BndDelta(Diff packageDiff, aQute.bnd.version.Version suggestedVersion) {
		this.packageDiff = packageDiff;
		this.version = suggestedVersion;
	}

	@Override
	public Version getSuggestedVersion() {
		return new Version(this.version.toString());
	}

	@Override
	public String getDescription() {
		return packageDiff.toString();
	}

}
