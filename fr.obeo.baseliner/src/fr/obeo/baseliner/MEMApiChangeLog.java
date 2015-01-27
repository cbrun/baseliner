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
		StringBuffer result = new StringBuffer();
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			result.append("\n\nh4. Changes in @" + entry.getKey() + "@\n\n");
//			result.append("\n * The " + entry.getKey() + "[" + entry.getValue().getOldVersion() + "]" + "-> ["
//					+ entry.getValue().getSuggestedVersion() + "]");
			result.append(entry.getValue().getDescription());
		}
		return result.toString();
	}

}
