package fr.obeo.baseliner.semver;

import org.osgi.framework.Version;

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

}
