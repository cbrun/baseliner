package fr.obeo.baseliner.bnd;

import java.util.List;

import org.osgi.framework.Version;

import aQute.bnd.differ.Baseline.Info;
import aQute.bnd.service.diff.Diff;
import aQute.bnd.service.diff.Type;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import fr.obeo.baseliner.Delta;

public class BndDelta implements Delta {

	private Info info;

	public BndDelta(Info info) {
		this.info = info;
	}

	@Override
	public Optional<Version> getSuggestedVersion() {
		if (this.info.suggestedVersion != null) {
			return Optional.of(new Version(this.info.suggestedVersion.toString()));
		} else {
			return Optional.<Version> absent();
		}
	}

	public Optional<Version> getOldVersion() {
		if (this.info.olderVersion != null) {
			return Optional.of(new Version(this.info.olderVersion.toString()));
		} else {
			return Optional.<Version> absent();
		}
	}

	public Optional<Version> getNewVersion() {
		if (this.info.newerVersion != null) {
			return Optional.of(new Version(this.info.newerVersion.toString()));
		} else {
			return Optional.<Version> absent();
		}
	}

	@Override
	public String getBreakingAPIChanges() {
		StringBuffer report = new StringBuffer();
		for (Diff diff : getBreakingChanges()) {
			report.append(show(diff, "  ", true));
		}
		return report.toString();
	}

	@Override
	public String getCompatibleAPIChanges() {
		StringBuffer report = new StringBuffer();
		for (Diff diff : getCompatibleChanges()) {
			report.append(show(diff, "  ", true));
		}
		return report.toString();
	}

	private List<Diff> getBreakingChanges() {
		List<Diff> breakingChanges = Lists.newArrayList();
		for (Diff child : info.packageDiff.getChildren()) {
			if (child.getDelta() == aQute.bnd.service.diff.Delta.MAJOR) {
				breakingChanges.add(child);
			}
		}
		return breakingChanges;
	}

	private List<Diff> getCompatibleChanges() {
		List<Diff> compatible = Lists.newArrayList();
		for (Diff child : info.packageDiff.getChildren()) {
			if (child.getDelta() == aQute.bnd.service.diff.Delta.MINOR
					|| child.getDelta() == aQute.bnd.service.diff.Delta.MICRO) {
				compatible.add(child);
			}
		}
		return compatible;
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
