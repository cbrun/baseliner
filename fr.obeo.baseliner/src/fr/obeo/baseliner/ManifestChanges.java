package fr.obeo.baseliner;

import java.util.Map;

import org.osgi.framework.Version;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class ManifestChanges {

	public static final ManifestChanges NOCHANGE = new ManifestChanges(Maps.<String, Delta> newHashMap(),
			Optional.<String> absent(), Optional.<Version> absent());

	private Optional<String> fileContent;

	private Map<String, Delta> changes;

	private Optional<Version> newVersion;

	public ManifestChanges(Map<String, Delta> changes, Optional<String> fileContent, Optional<Version> newVersion) {
		super();
		this.changes = changes;
		this.fileContent = fileContent;
		this.newVersion = newVersion;
	}

	public Optional<String> getFileContent() {
		return fileContent;
	}

	public Map<String, Delta> getChanges() {
		return changes;
	}

	public boolean bundleVersionUpdate() {
		return newVersion.isPresent();
	}

}
