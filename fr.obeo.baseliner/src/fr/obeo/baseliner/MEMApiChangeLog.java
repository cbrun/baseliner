package fr.obeo.baseliner;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class MEMApiChangeLog implements ApiChangeLog {

	private Map<String, Delta> mergedChanges = Maps.newHashMap();

	@Override
	public void aggregate(String projectName, Map<String, Delta> changes) {
		mergedChanges.putAll(changes);
	}

	public String report(ReportFormat formatter) {
		StringBuffer breakingChangesPart = new StringBuffer();
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			String changeDescription = entry.getValue().getBreakingAPIChanges(formatter);
			if (changeDescription != null && changeDescription.length() > 0) {
				breakingChangesPart.append(formatter.packageSection(entry.getKey()));
				breakingChangesPart.append(changeDescription);
			}
		}

		StringBuffer compatibleChangesPart = new StringBuffer();
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			String changeDescription = entry.getValue().getCompatibleAPIChanges(formatter);
			if (changeDescription != null && changeDescription.length() > 0) {
				compatibleChangesPart.append(formatter.packageSection(entry.getKey()));
				compatibleChangesPart.append(changeDescription);
			}
		}
		StringBuffer report = new StringBuffer();
		if (breakingChangesPart.length() > 0) {
			report.append(formatter.section("Incompatible API Changes"));
			report.append(breakingChangesPart.toString());
		}
		if (compatibleChangesPart.length() > 0) {
			report.append(formatter.section("Compatible API Changes"));
			report.append(compatibleChangesPart.toString());
		}

		return report.toString();
	}

}
