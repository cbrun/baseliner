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
import com.google.common.collect.Maps;

public class PluginBaseliner {

	private Optional<BaselinerJarProvider> jarProvider;

	private Optional<ApiComparator> apiComparator;

	private ManifestCleanup cleanup = new NOOPManifestCleanup();

	public synchronized void setBaselineJarProvider(BaselinerJarProvider jarProvider) {
		this.jarProvider = Optional.of(jarProvider);
	}

	public synchronized void unsetBaselineJarProvider(BaselinerJarProvider jarProvider) {
		if (this.jarProvider.isPresent() && this.jarProvider.get() == jarProvider) {
			this.jarProvider = Optional.absent();
		}
	}

	public synchronized void setManifestCleanup(ManifestCleanup cleanup) {
		this.cleanup = cleanup;
	}

	public synchronized void unsetManifestCleanup(ManifestCleanup cleanup) {
		if (this.cleanup == cleanup) {
			this.cleanup = new NOOPManifestCleanup();
		}
	}

	// Method will be used by DS to set the quote service
	public synchronized void setApiComparator(ApiComparator apiComparator) {
		this.apiComparator = Optional.of(apiComparator);
	}

	// Method will be used by DS to unset the quote service
	public synchronized void unsetApiComparator(ApiComparator apiComparator) {
		if (this.apiComparator.isPresent() && this.apiComparator.get() == apiComparator) {
			this.apiComparator = Optional.absent();
		}
	}

	public PluginBaseliner() {
	}

	public ManifestChanges updateManifestFile(File manifestFile, Collection<File> outputDirs, Collection<File> srcDirs)
			throws FileNotFoundException {
		ManifestChanges result = addExportedPackagesVersions(manifestFile, outputDirs);
		cleanup.cleanup(manifestFile);
		return result;
	}

	private ManifestChanges addExportedPackagesVersions(File manifestFile, Collection<File> outputDirs)
			throws FileNotFoundException {
		Map<String, Delta> result = Maps.newHashMap();
		if (apiComparator.isPresent()) {
			for (File outputDirectory : outputDirs) {
				apiComparator.get().loadNewClassesFromFolder(outputDirectory.getParentFile(), outputDirectory);
			}
			FileInputStream fileInputStream = new FileInputStream(manifestFile);
			try {
				ManifestHandler manifestHandler = new ManifestHandler();
				manifestHandler.load(fileInputStream);
				if (jarProvider.isPresent()) {
					File jar = jarProvider.get().getPreviousJar(manifestHandler.getSymbolicName(),
							manifestHandler.getBundleVersion());
					if (jar != null) {
						apiComparator.get().loadOldClassesFromFolder(jar);
					}
				}
				result = apiComparator.get().diffByPackage();

				for (Entry<String, Version> exportedPackage : manifestHandler.getExportedPackages().entrySet()) {

					String ns = exportedPackage.getKey();

					// System.out
					// .println("===================================");
					// System.err.println("delta for " + ns);
					// System.err.println("specified version for package is "
					// + exportedPackage.getValue());

					Delta packageDelta = result.get(ns);
					if (packageDelta != null) {
						Version inferedVersion = packageDelta.getSuggestedVersion();
						System.out.println("==== " + ns + " package infered version : " + inferedVersion);
						// dumpNSCompat(result.get(ns));
						manifestHandler.setPackageVersion(ns, inferedVersion);
					} else {
						// System.out.println("no delta.");
					}
					// dumpNSCompat(result, ns);
				}
				
				manifestHandler.setNewBundleVersion(manifestHandler.getHighestExportedVersion());
				
				Optional<String> newContent = manifestHandler.update(manifestFile);
				return new ManifestChanges(result, newContent);
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
		return ManifestChanges.NOCHANGE;
	}

}
