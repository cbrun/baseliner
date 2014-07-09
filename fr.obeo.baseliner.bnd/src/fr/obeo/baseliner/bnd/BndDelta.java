package fr.obeo.baseliner.bnd;

import org.osgi.framework.Version;

import aQute.bnd.differ.Baseline.Info;
import aQute.bnd.service.diff.Diff;
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
		String description = info.packageDiff.toString();
		description += show(info.packageDiff, " ", true);
		return description;
	}

	/**
	 * Show the diff recursively
	 *
	 * @param p
	 * @param i
	 */
	private String show(Diff p, String indent, boolean warning) {
		String result = "";
		aQute.bnd.service.diff.Delta d = p.getDelta();
		if (d == aQute.bnd.service.diff.Delta.UNCHANGED)
			return result;

			result += "\n" + indent + p ;			

		indent = indent + "  ";
		switch (d) {
		case CHANGED:
		case MAJOR:
		case MINOR:
		case MICRO:
			break;

		default:
			return result;
		}
		for (Diff c : p.getChildren())
			result += show(c, indent, warning) ;
		return result;
	}

}
