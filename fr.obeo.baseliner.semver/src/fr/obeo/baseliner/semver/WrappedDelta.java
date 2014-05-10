package fr.obeo.baseliner.semver;

import org.osgi.framework.Version;

import fr.obeo.baseliner.Delta;

public class WrappedDelta implements Delta {

	private org.semver.Delta wrapped;

	public WrappedDelta(org.semver.Delta semVerDelta) {
		this.wrapped = semVerDelta;
	}

	@Override
	public Version infer(Version oldPackageVersion) {
		return toOSGIVersion(this.wrapped
				.infer(toSemVersion(oldPackageVersion)));
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
