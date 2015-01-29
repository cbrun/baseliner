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

	public String report() {
		StringBuffer breakingChangesPart = new StringBuffer();
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			String changeDescription = entry.getValue().getBreakingAPIChanges();
			if (changeDescription != null && changeDescription.length() > 0) {
				breakingChangesPart.append("\n\nh3. Package @" + entry.getKey() + "@\n\n");
				breakingChangesPart.append(changeDescription);
			}
		}

		StringBuffer compatibleChangesPart = new StringBuffer();
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			String changeDescription = entry.getValue().getCompatibleAPIChanges();
			if (changeDescription != null && changeDescription.length() > 0) {
				compatibleChangesPart.append("\n\nh3. Package @" + entry.getKey() + "@\n\n");
				compatibleChangesPart.append(changeDescription);
			}
		}
		StringBuffer report = new StringBuffer();
		if (breakingChangesPart.length() > 0) {
			report.append("\n\nh2. Breaking API Changes :\n\n");
			report.append(breakingChangesPart.toString());
		}
		if (compatibleChangesPart.length() > 0) {
			report.append("\n\nh2. Compatible API Changes :\n\n");
			report.append(compatibleChangesPart.toString());
		}

		return report.toString();
	}

}
