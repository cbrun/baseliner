package fr.obeo.baseliner.semver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Version;
import org.osjava.jardiff.AbstractInfo;
import org.osjava.jardiff.ClassInfo;
import org.semver.Delta.Change;
import org.semver.Delta.Difference;

import fr.obeo.baseliner.Delta;

public class WrappedDelta implements Delta {

	private org.semver.Delta wrapped;
	private org.semver.Version oldVersion;

	public WrappedDelta(org.semver.Delta semVerDelta,
			org.semver.Version oldVersion) {
		this.wrapped = semVerDelta;
		this.oldVersion = oldVersion;
	}

	@Override
	public Version getSuggestedVersion() {
		return toOSGIVersion(this.wrapped.infer(oldVersion));
	}

	public static Version toOSGIVersion(org.semver.Version oldPackageVersion) {
		if (oldPackageVersion != null) {
			return new Version(oldPackageVersion.toString());
		}
		return null;
	}

	public static org.semver.Version toSemVersion(Version oldPackageVersion) {
		if (oldPackageVersion != null) {
			org.semver.Version result = new org.semver.Version(
					oldPackageVersion.getMajor(), oldPackageVersion.getMinor(),
					oldPackageVersion.getMicro());
			return result;
		}
		return null;
	}

	@Override
	public String getDescription() {
		String result = "";
		final List<Difference> sortedDifferences = new LinkedList<Difference>(
				wrapped.getDifferences());
		Collections.sort(sortedDifferences);
		String currentClassName = "";
		for (final Difference difference : sortedDifferences) {
			if (!currentClassName.equals(difference.getClassName())) {
				result += "Class " + difference.getClassName() + "\n";
			}
			result += " " + extractActionType(difference) + " "
					+ extractInfoType(difference.getInfo()) + " "
					+ extractDetails(difference) + "\n";
			currentClassName = difference.getClassName();
		}
		return result;
	}

	protected static String extractActionType(final Difference difference) {
		final String actionType = difference.getClass().getSimpleName();
		return actionType.endsWith("e") ? actionType + "d" : actionType + "ed";
	}

	protected static String extractInfoType(final AbstractInfo info) {
		final String simpleClassName = info.getClass().getSimpleName();
		return simpleClassName.substring(0, simpleClassName.indexOf("Info"));
	}

	protected static String extractDetails(final Difference difference) {
		if (difference instanceof Change) {
			final Change change = (Change) difference;
			return extractDetails(difference.getInfo())
					+ " "
					+ extractAccessDetails(difference.getInfo(),
							change.getModifiedInfo());
		} else {
			return extractDetails(difference.getInfo());
		}
	}

	protected static String extractDetails(final AbstractInfo info) {
		final StringBuilder builder = new StringBuilder();
		if (!(info instanceof ClassInfo)) {
			builder.append(info.getName());
		}
		return builder.toString();
	}

	protected static void accumulateAccessDetails(final String access,
			final boolean previousAccess, final boolean currentAccess,
			final List<String> added, final List<String> removed) {
		if (previousAccess != currentAccess) {
			if (previousAccess) {
				removed.add(access);
			} else {
				added.add(access);
			}
		}
	}

	protected static String extractAccessDetails(
			final AbstractInfo previousInfo, final AbstractInfo currentInfo) {
		final List<String> added = new LinkedList<String>();
		final List<String> removed = new LinkedList<String>();
		accumulateAccessDetails("abstract", previousInfo.isAbstract(),
				currentInfo.isAbstract(), added, removed);
		accumulateAccessDetails("annotation", previousInfo.isAnnotation(),
				currentInfo.isAnnotation(), added, removed);
		accumulateAccessDetails("bridge", previousInfo.isBridge(),
				currentInfo.isBridge(), added, removed);
		accumulateAccessDetails("enum", previousInfo.isEnum(),
				currentInfo.isEnum(), added, removed);
		accumulateAccessDetails("final", previousInfo.isFinal(),
				currentInfo.isFinal(), added, removed);
		accumulateAccessDetails("interface", previousInfo.isInterface(),
				currentInfo.isInterface(), added, removed);
		accumulateAccessDetails("native", previousInfo.isNative(),
				currentInfo.isNative(), added, removed);
		accumulateAccessDetails("package-private",
				previousInfo.isPackagePrivate(),
				currentInfo.isPackagePrivate(), added, removed);
		accumulateAccessDetails("private", previousInfo.isPrivate(),
				currentInfo.isPrivate(), added, removed);
		accumulateAccessDetails("protected", previousInfo.isProtected(),
				currentInfo.isProtected(), added, removed);
		accumulateAccessDetails("public", previousInfo.isPublic(),
				currentInfo.isPublic(), added, removed);
		accumulateAccessDetails("static", previousInfo.isStatic(),
				currentInfo.isStatic(), added, removed);
		accumulateAccessDetails("strict", previousInfo.isStrict(),
				currentInfo.isStrict(), added, removed);
		accumulateAccessDetails("super", previousInfo.isSuper(),
				currentInfo.isSuper(), added, removed);
		accumulateAccessDetails("synchronized", previousInfo.isSynchronized(),
				currentInfo.isSynchronized(), added, removed);
		accumulateAccessDetails("synthetic", previousInfo.isSynthetic(),
				currentInfo.isSynthetic(), added, removed);
		accumulateAccessDetails("transcient", previousInfo.isTransient(),
				currentInfo.isTransient(), added, removed);
		accumulateAccessDetails("varargs", previousInfo.isVarargs(),
				currentInfo.isVarargs(), added, removed);
		accumulateAccessDetails("volatile", previousInfo.isVolatile(),
				currentInfo.isVolatile(), added, removed);
		final StringBuilder details = new StringBuilder();
		if (!added.isEmpty()) {
			details.append("added: ");
			for (final String access : added) {
				details.append(access).append(" ");
			}
		}
		if (!removed.isEmpty()) {
			details.append("removed: ");
			for (final String access : removed) {
				details.append(access).append(" ");
			}
		}
		return details.toString().trim();
	}

}
