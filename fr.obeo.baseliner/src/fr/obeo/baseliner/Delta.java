package fr.obeo.baseliner;

import org.osgi.framework.Version;

import com.google.common.base.Optional;

public interface Delta {

	Optional<Version> getSuggestedVersion();

	String getBreakingAPIChanges(ReportFormat formatter);
	
	String getCompatibleAPIChanges(ReportFormat formatter);

	Optional<Version> getOldVersion();

	Optional<Version> getNewVersion();	

}
