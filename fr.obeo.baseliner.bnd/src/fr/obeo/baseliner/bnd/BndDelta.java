package fr.obeo.baseliner.bnd;

import java.util.Collection;
import java.util.List;

import org.osgi.framework.Version;

import aQute.bnd.differ.Baseline.Info;
import aQute.bnd.service.diff.Diff;
import aQute.bnd.service.diff.Type;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.obeo.baseliner.Delta;
import fr.obeo.baseliner.ReportFormat;

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
	public String getBreakingAPIChanges(ReportFormat format) {
		return prettyPrint(getBreakingChanges(), format);
	}

	@Override
	public String getCompatibleAPIChanges(ReportFormat format) {
		return prettyPrint(getCompatibleChanges(), format);
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

	private String prettyPrint(Collection<Diff> diffs, ReportFormat format) {
		StringBuffer result = new StringBuffer();
		for (Diff diff : diffs) {
			if (diff.getType() != Type.PACKAGE && diff.getType() != Type.VERSION
					&& diff.getDelta() != aQute.bnd.service.diff.Delta.UNCHANGED) {
				prettyPrintDiff(result, diff, 1, format);
			}
		}
		return result.toString();

	}

	private void prettyPrintDiff(StringBuffer result, Diff diff, int deph, ReportFormat format) {
		List<? extends Diff> changedChildrens = getChangedChildrens(diff);
		if (changedChildrens.size() > 0) {
			result.append(format.startList(deph, prettyType(diff), diff.getName()));
			for (Diff child : changedChildrens) {
				result.append(format.beginListItem(deph));
				prettyPrintDiff(result, child, deph + 1, format);
				result.append(format.endListItem(deph));
			}
			result.append(format.endList(deph));
		} else {
			result.append(format.change(deph, diff.getName(), prettyType(diff), prettyChange(diff.getDelta())));
		}
	}

	private List<? extends Diff> getChangedChildrens(Diff diff) {
		return Lists.newArrayList(Iterables.filter(diff.getChildren(), new Predicate<Diff>() {

			@Override
			public boolean apply(Diff arg0) {
				return arg0.getDelta() != aQute.bnd.service.diff.Delta.UNCHANGED;
			}
		}));
	}

	private String prettyChange(aQute.bnd.service.diff.Delta d) {
		String pretty = d.toString();
		switch (d) {
		case ADDED:
			pretty = "added";
			break;
		case REMOVED:
			pretty = "removed";
			break;
		case UNCHANGED:
			pretty = "unchanged";
			break;
		case MAJOR:
			pretty = "changed in an incompatible way";
			break;
		case MICRO:
			pretty = "changed";
			break;
		case MINOR:
			pretty = "extended";
			break;

		default:
			break;
		}
		return pretty;
	}

	private String prettyType(Diff current) {
		Type type = current.getType();
		if (type != null && type.toString().length() > 1) {
			String result = type.toString().toLowerCase();
			switch (type) {
			case IMPLEMENTS:
				result = "interface implementation";
				break;
			case EXTENDS:
				result = "inheritance";
				break;
			case RETURN:
				result = "return type";
				break;
			case ACCESS:
				result = "modifier";
				break;
			default:
				break;
			}
			return result;
		}
		return "?";
	}

}
