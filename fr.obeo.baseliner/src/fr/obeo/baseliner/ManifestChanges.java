package fr.obeo.baseliner;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class ManifestChanges {

	public static final ManifestChanges NOCHANGE = new ManifestChanges(
			Maps.<String, Delta> newHashMap(), Optional.<String> absent());

	private Optional<String> fileContent;

	private Map<String, Delta> changes;

	public ManifestChanges(Map<String, Delta> changes,
			Optional<String> fileContent) {
		super();
		this.changes = changes;
		this.fileContent = fileContent;
	}

	public Optional<String> getFileContent() {
		return fileContent;
	}

	public Map<String, Delta> getChanges() {
		return changes;
	}

}
