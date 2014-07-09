package fr.obeo.baseliner;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class MEMApiChangeLog implements ApiChangeLog {

	private Map<String, Delta> mergedChanges = Maps.newHashMap();

	@Override
	public void aggregate(String projectName, Map<String, Delta> changes) {
		mergedChanges.putAll(changes);
		System.err.println("project:" + projectName );
		dump();
	}

	private void dump() {
		for (Entry<String, Delta> entry : mergedChanges.entrySet()) {
			System.out.println("package :" + entry.getKey() + "[" + entry.getValue().getOldVersion() + "]" + "-> [" + entry.getValue().getSuggestedVersion() + "]");
			System.out.println("        " + entry.getValue().getDescription());
		}
	}

}
