package fr.obeo.baseliner;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Version;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ManifestChanges {

	public static final ManifestChanges NOCHANGE = new ManifestChanges(Maps.<String, Delta> newHashMap(),
			Optional.<String> absent(), Optional.<Version> absent());

	private Optional<String> fileContent;

	private Map<String, Delta> changes;

	private Optional<Version> newVersion;

	private Collection<IStatus> statuses = Lists.newArrayList();

	public ManifestChanges(Map<String, Delta> changes, Optional<String> fileContent, Optional<Version> newVersion) {
		super();
		this.changes = changes;
		this.fileContent = fileContent;
		this.newVersion = newVersion;
	}

	public Optional<String> getUpdatedManifestContent() {
		return fileContent;
	}

	public Map<String, Delta> getPackageChanges() {
		return changes;
	}

	public boolean bundleVersionUpdate() {
		return newVersion.isPresent();
	}

	public void addStatus(IStatus status) {
		this.statuses.add(status);
	}

	public Collection<IStatus> getStatuses() {
		return statuses;
	}

}
