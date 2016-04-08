package fr.obeo.baseliner.ui;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.obeo.baseliner.ui.builder.BaselinerBuilderListener;
import fr.obeo.baseliner.ui.builder.BaselinerNature;

public class WorkspaceBuilder {

	private static final String PREF_WORKSPACE_BUILDER_ENABLED_STATE = BaselinerUIPlugin.ID
			+ ".PREF_WORKSPACE_BUILDER_ENABLED_STATE";
	private Collection<BaselinerBuilderListener> listeners = Sets.newLinkedHashSet();

	public void addListener(BaselinerBuilderListener lis) {
		listeners.add(lis);
	}

	public void removeListener(BaselinerBuilderListener lis) {
		listeners.remove(lis);
	}

	public void setEnablement(boolean value) {
		boolean enablement = isEnabled();
		if (value != enablement) {
			setBoolean(BaselinerUIPlugin.ID, PREF_WORKSPACE_BUILDER_ENABLED_STATE, value, null);

			for (BaselinerBuilderListener l : listeners) {
				l.enablementChanged(value);
			}
			if (value) {
				Job build = new Job("Check semantic versioning") {

					@Override
					protected IStatus run(IProgressMonitor pm) {
						Collection<IProject> prjs = Lists.newArrayList();
						for (IProject prj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
							if (BaselinerNature.isConfigured(prj)) {
								prjs.add(prj);
							}
						}

						pm.beginTask("Building projects", prjs.size());

						/*
						 * we might want baseline every project which is
						 * configured.
						 */
						for (IProject prj : prjs) {
							if (pm.isCanceled()) {
								break;
							}
							try {
								prj.build(IncrementalProjectBuilder.FULL_BUILD, pm);
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							pm.worked(1);
						}
						pm.done();
						return Status.OK_STATUS;
					}
				};
				build.schedule();
			}
		}
	}

	public boolean isEnabled() {
		return Platform.getPreferencesService().getBoolean(BaselinerUIPlugin.ID, PREF_WORKSPACE_BUILDER_ENABLED_STATE,
				true, null);
	}

	static final IScopeContext[] contexts = new IScopeContext[] { DefaultScope.INSTANCE, InstanceScope.INSTANCE };

	static final int DEFAULT_CONTEXT = 0;
	static final int INSTANCE_CONTEXT = 1;

	/**
	 * Sets a boolean preference in the {@link InstanceScope} or the given
	 * {@link IScopeContext} if it is not <code>null</code>. Preferences set in
	 * a given context are flushed as they are set.
	 * 
	 * @param qualifier
	 *            the preference qualifier
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param context
	 *            the context to set the value in
	 */
	public static synchronized void setBoolean(String qualifier, String key, boolean value, IScopeContext context) {
		if (context != null) {
			try {
				IEclipsePreferences node = context.getNode(qualifier);
				node.putBoolean(key, value);
				node.flush();
			} catch (BackingStoreException bse) {

			}
		} else {
			contexts[INSTANCE_CONTEXT].getNode(qualifier).putBoolean(key, value);
		}
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the global viewpoints registry.
	 */
	public static WorkspaceBuilder getInstance() {
		return WorkspaceBuilderHolder.instance;
	}

	/*
	 * This class *will not* be initialized (hence the static block not
	 * triggered) until the getInstance() method is called. This is actually the
	 * 'Initialization on demand Holder' pattern which is both thread safe,
	 * lazy, and does not introduce extra synchronization locks.
	 */
	private static class WorkspaceBuilderHolder {
		private static WorkspaceBuilder instance;

		static {
			instance = new WorkspaceBuilder();
		}
	}
}
