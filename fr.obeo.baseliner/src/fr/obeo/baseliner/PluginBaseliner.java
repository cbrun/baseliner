package fr.obeo.baseliner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Version;

import com.google.common.base.Optional;

public class PluginBaseliner {

	private Optional<BaselinerJarProvider> jarProvider;

	private Optional<ApiComparator> apiComparator;

	// Method will be used by DS to set the quote service
	public synchronized void setBaselineJarProvider(
			BaselinerJarProvider jarProvider) {
		this.jarProvider = Optional.of(jarProvider);
	}

	// Method will be used by DS to unset the quote service
	public synchronized void unsetBaselineJarProvider(
			BaselinerJarProvider jarProvider) {
		if (this.jarProvider.isPresent()
				&& this.jarProvider.get() == jarProvider) {
			this.jarProvider = Optional.absent();
		}
	}

	// Method will be used by DS to set the quote service
	public synchronized void setApiComparator(ApiComparator apiComparator) {
		this.apiComparator = Optional.of(apiComparator);
	}

	// Method will be used by DS to unset the quote service
	public synchronized void unsetApiComparator(ApiComparator apiComparator) {
		if (this.apiComparator.isPresent()
				&& this.apiComparator.get() == apiComparator) {
			this.apiComparator = Optional.absent();
		}
	}

	public PluginBaseliner() {
	}

	public String updateManifestFile(File manifestFile,
			Collection<File> outputDirs, Collection<File> srcDirs) throws FileNotFoundException {
		String diffReport = null;
		if (apiComparator.isPresent()) {
			for (File outputDirectory : outputDirs) {
				apiComparator.get().loadNewClassesFromFolder(outputDirectory);
			}
			FileInputStream fileInputStream = new FileInputStream(manifestFile);
			try {
				ManifestHandler manifestHandler = new ManifestHandler();
				manifestHandler.load(fileInputStream);
				if (jarProvider.isPresent()) {
					File jar = jarProvider.get().getPreviousJar(
							manifestHandler.getSymbolicName(),
							manifestHandler.getBundleVersion());
					if (jar != null) {
						apiComparator.get().loadOldClassesFromFolder(jar);
					}
				}
				Map<String, Delta> result = apiComparator.get().diffByPackage();

				for (Entry<String, Version> exportedPackage : manifestHandler
						.getExportedPackages().entrySet()) {

					String ns = exportedPackage.getKey();

					// System.out
					// .println("===================================");
					// System.err.println("delta for " + ns);
					// System.err.println("specified version for package is "
					// + exportedPackage.getValue());

					Delta packageDelta = result.get(ns);
					if (packageDelta != null) {
						if (apiComparator.get().getOldPackageVersion(ns) != null) {
							Version inferedVersion = packageDelta
									.infer(apiComparator.get()
											.getOldPackageVersion(ns));
							if (inferedVersion.compareTo(apiComparator.get()
									.getOldPackageVersion(ns)) != 0) {
								System.out.println("==== "
										+ ns
										+ " package version : "
										+ apiComparator.get()
												.getOldPackageVersion(ns)
										+ " but infered version is :"
										+ inferedVersion);
								// dumpNSCompat(result.get(ns));
							}
							manifestHandler.setPackageVersion(ns,
									inferedVersion);
						} else {
							/*
							 * we did not find the old specified version. It
							 * just mean the package was not exported yet.
							 */
							// System.err
							// .println("could not retrieve the old specified version.");
						}
					} else {
						// System.out.println("no delta.");
					}
					// dumpNSCompat(result, ns);
				}

				manifestHandler.update(manifestFile);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return diffReport;
	}

}
