package fr.obeo.baseliner;

import java.util.Map;

public interface ApiChangeLog {

	void aggregate(String projectName, Map<String, Delta> changes) ;

}
