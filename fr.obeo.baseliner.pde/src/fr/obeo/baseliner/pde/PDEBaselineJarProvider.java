package fr.obeo.baseliner.pde;

import java.io.File;

import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;

import fr.obeo.baseliner.BaselinerJarProvider;

public class PDEBaselineJarProvider implements BaselinerJarProvider {

	@Override
	public File getPreviousJar(String symbolicName, String bundleVersion) {
		for (IApiBaseline baseline : ApiPlugin.getDefault().getApiBaselineManager()
				.getApiBaselines()) {
			IApiComponent bundle = baseline.getApiComponent(symbolicName);
			if (bundle != null && bundle.getLocation() != null) {
				File jarFile = new File(bundle.getLocation());
				if (jarFile.exists()) {
					return jarFile;
				}
			}
			
		}
		return null;
	}
}
