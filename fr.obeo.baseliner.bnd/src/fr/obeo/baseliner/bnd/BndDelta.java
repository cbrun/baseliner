package fr.obeo.baseliner.bnd;

import org.osgi.framework.Version;

import aQute.bnd.differ.Baseline.Info;
import fr.obeo.baseliner.Delta;

public class BndDelta implements Delta {

	private Info info;

	public BndDelta(Info info) {
		this.info = info;
	}

	@Override
	public Version getSuggestedVersion() {
		return new Version(this.info.suggestedVersion.toString());
	}
	
	public Version getOldVersion() {
		return new Version(this.info.olderVersion.toString());
	}

	@Override
	public String getDescription() {		
		return info.packageDiff.toString();
	}

}
