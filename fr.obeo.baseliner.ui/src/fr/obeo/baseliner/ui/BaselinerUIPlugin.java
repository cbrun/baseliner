package fr.obeo.baseliner.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import fr.obeo.baseliner.PluginBaseliner;

public class BaselinerUIPlugin extends AbstractUIPlugin {

	public PluginBaseliner baseliner;

	public ServiceReference<?> serviceReference;


	/** The shared instance. */
	private static BaselinerUIPlugin plugin;
	
	
	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static BaselinerUIPlugin getDefault() {
		return plugin;
	}
	public BaselinerUIPlugin() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		serviceReference = context.getServiceReference(PluginBaseliner.class
				.getName());
		baseliner = (PluginBaseliner) context.getService(serviceReference);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		if (serviceReference != null) {
			context.ungetService(serviceReference);
		}
	}

}
