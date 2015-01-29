package fr.obeo.baseliner.ui.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class BaselinerPreferences {

	public static final String DEFAULT_JAR_SOURCE = "platform:/pde/apibaselines";

	public static final String KEY_JAR_SOURCE = "baselineSource";

	public static final String PREFERENCES_QUALIFIER = "fr.obeo.baseliner";

	public static String getJarProviderSource(IProject project) {
		IPreferencesService service = Platform.getPreferencesService();
		String[] lookupOrder = new String[] { ProjectScope.SCOPE, InstanceScope.SCOPE };
		service.setDefaultLookupOrder(PREFERENCES_QUALIFIER, KEY_JAR_SOURCE, lookupOrder);
		IScopeContext[] contexts = new IScopeContext[] { new ProjectScope(project) };
		String prefValue = service.getString(PREFERENCES_QUALIFIER, KEY_JAR_SOURCE, DEFAULT_JAR_SOURCE, contexts);
		return prefValue;
	}

	public static void setJarProviderSources(IProject project, String value) throws BackingStoreException {
		IScopeContext context = new ProjectScope(project);
		IEclipsePreferences node = context.getNode(PREFERENCES_QUALIFIER);
		if (node != null) {
			node.put(KEY_JAR_SOURCE, value);
		}
		node.sync();
	}

}
