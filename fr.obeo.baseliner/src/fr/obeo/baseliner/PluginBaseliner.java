package fr.obeo.baseliner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Version;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PluginBaseliner {

	private Optional<BaselinerJarProvider> jarProvider;

	private Optional<ApiComparator> apiComparator;

	private ManifestCleanup cleanup = new NOOPManifestCleanup();

	private String jarProviderSource = "platform:/pde/apibaselines";

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

	public ManifestChanges updateManifestFile(File manifestFile, Collection<File> outputDirs, Collection<File> srcDirs,
			IProgressMonitor monitor) throws FileNotFoundException {
		ManifestChanges result = addExportedPackagesVersions(manifestFile, outputDirs, monitor);
		if (!monitor.isCanceled()) {
			cleanup.cleanup(manifestFile);
		}
		return result;
	}

	private static final Version NONE = new Version(0, 0, 0);

	private ManifestChanges addExportedPackagesVersions(File manifestFile, Collection<File> outputDirs,
			IProgressMonitor monitor) throws FileNotFoundException {
		Map<String, Delta> result = Maps.newHashMap();
		if (apiComparator.isPresent()) {
			for (File outputDirectory : outputDirs) {
				apiComparator.get().loadNewClassesFromFolder(outputDirectory.getParentFile(), outputDirectory);
			}
			FileInputStream fileInputStream = new FileInputStream(manifestFile);
			List<IStatus> statusesToReport = Lists.newArrayList();
			try {
				ManifestRewriter manifestHandler = new ManifestRewriter();
				manifestHandler.load(fileInputStream);
				if (jarProvider.isPresent()) {
					File jar = jarProvider.get().getPreviousJar(jarProviderSource, manifestHandler.getSymbolicName(),
							manifestHandler.getBundleVersion(), monitor, statusesToReport);
					if (jar != null) {
						apiComparator.get().loadOldClassesFromFolder(jar);
					} else {
						statusesToReport
								.add(new Status(IStatus.ERROR, "fr.obeo.baseliner", "Could not find a "
										+ manifestHandler.getSymbolicName() + " bundle in the baseline : "
										+ jarProviderSource));
						ManifestChanges manifestChanges = new ManifestChanges(result, Optional.<String> absent(),
								Optional.<Version> absent());
						for (IStatus iStatus : statusesToReport) {
							manifestChanges.addStatus(iStatus);
						}
						return manifestChanges;
					}
				}
				result = apiComparator.get().diffByPackage();

				for (Entry<String, Version> exportedPackage : manifestHandler.getExportedPackages().entrySet()) {

					String ns = exportedPackage.getKey();

					Delta d = result.get(ns);
					if (d != null) {
						Version inferedVersion = new Version(manifestHandler.getBundleVersion());
						if (d.getNewVersion().isPresent()) {
							inferedVersion = d.getNewVersion().get();
						}
						/*
						 * if there is no suggested version (or it is 0.0.0,
						 * then pick the bundle version.
						 */
						if (d.getSuggestedVersion().isPresent() && !d.getSuggestedVersion().get().equals(NONE)) {
							inferedVersion = d.getSuggestedVersion().get();
						}
						manifestHandler.setPackageVersion(ns, inferedVersion);
					}
				}

				Version bundleVersionToSet = manifestHandler.getHighestExportedVersion();
				manifestHandler.setNewBundleVersion(bundleVersionToSet);

				Optional<String> newContent = manifestHandler.update(manifestFile);
				boolean bundleVersionIsChanging = !bundleVersionToSet.toString().equals(
						manifestHandler.getBundleVersion());

				if (bundleVersionIsChanging && manifestFile.getParentFile() != null
						&& manifestFile.getParentFile().getParentFile() != null && !monitor.isCanceled()) {
					File pomXML = new File(manifestFile.getParentFile().getParentFile().getAbsolutePath() + "/pom.xml");
					if (pomXML.exists() && pomXML.canRead() && pomXML.canWrite()) {
						try {
							PomRewriter pomRewriter = new PomRewriter();
							pomRewriter.setPluginVersion(bundleVersionToSet);
							pomRewriter.rewrite(pomXML);
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if (bundleVersionIsChanging) {
					ManifestChanges manifestChange = new ManifestChanges(result, newContent,
							Optional.of(bundleVersionToSet));
					for (IStatus iStatus : statusesToReport) {
						manifestChange.addStatus(iStatus);
					}
					return manifestChange;
				} else {
					ManifestChanges manifestChange = new ManifestChanges(result, newContent,
							Optional.<Version> absent());
					for (IStatus iStatus : statusesToReport) {
						manifestChange.addStatus(iStatus);
					}
					return manifestChange;
				}
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

	public void setJarProviderSource(String sourceURI) {
		this.jarProviderSource = sourceURI;

	}

}
