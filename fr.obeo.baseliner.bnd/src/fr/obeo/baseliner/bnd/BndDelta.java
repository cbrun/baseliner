package fr.obeo.baseliner.bnd;

import java.util.List;

import org.osgi.framework.Version;

import aQute.bnd.differ.Baseline.Info;
import aQute.bnd.service.diff.Diff;
import aQute.bnd.service.diff.Type;

import com.google.common.collect.Lists;

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
		StringBuffer report = new StringBuffer();
		Diff root = info.packageDiff;
		List<Diff> breakingChanges = Lists.newArrayList();
		List<Diff> compatibleChanges = Lists.newArrayList();
		for (Diff child : root.getChildren()) {
			if (child.getDelta() == aQute.bnd.service.diff.Delta.MAJOR) {
				breakingChanges.add(child);
			} else {
				compatibleChanges.add(child);
			}
		}
		if (breakingChanges.size() > 0) {
			report.append("\n\nh4. Breaking API Changes:\n");
			for (Diff diff : breakingChanges) {
				report.append(show(diff, "  ", true));
			}
		}
		if (compatibleChanges.size() > 0) {
			report.append("\n\nh4. Compatible API Changes:\n");
			for (Diff diff : compatibleChanges) {
				report.append(show(diff, "  ", true));
			}
		}
		return report.toString();
		//return show(info.packageDiff, "  ", true);
	}

	/**
	 * Show the diff recursively
	 *
	 * @param p
	 * @param i
	 */
	private String show(Diff p, String indent, boolean warning) {
		String result = "";
		if (p.getType() != Type.PACKAGE && p.getType() != Type.VERSION) {
			aQute.bnd.service.diff.Delta d = p.getDelta();
			if (d == aQute.bnd.service.diff.Delta.UNCHANGED) {
				return result;
			}

			result += "\n" + indent + p;

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
		}
		for (Diff c : p.getChildren())
			result += show(c, indent, warning);
		return result;
	}

}
