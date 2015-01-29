package fr.obeo.baseliner;

import org.osgi.framework.Version;

import com.google.common.base.Optional;

public interface Delta {

	Optional<Version> getSuggestedVersion();

	String getBreakingAPIChanges();
	
	String getCompatibleAPIChanges();

	Optional<Version> getOldVersion();

	Optional<Version> getNewVersion();	

}
