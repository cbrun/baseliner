package fr.obeo.tools.osgi.baseliner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;

import org.semver.Version;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

public class ManifestHandler {

	private String bundleVersion;

	private Map<String, Version> exportedPackages = Maps.newHashMap();

	private Multimap<String, String> extraExtensions = HashMultimap.create();

	public ManifestHandler() {

	}

	public Map<String, Version> getExportedPackages() {
		return exportedPackages;
	}

	public void load(InputStream in) throws IOException {
		Manifest manifest = new Manifest(in);
		bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");
		String exportedPAckages = manifest.getMainAttributes().getValue(
				"Export-Package");
		if (exportedPAckages != null) {
			Iterable<String> exportDirectives = Splitter.on(',').trimResults()
					.split(exportedPAckages);
			for (String directive : exportDirectives) {
				String manifestPackageVersion = bundleVersion;
				Iterator<String> splittedDirective = Splitter.on(';')
						.trimResults().split(directive).iterator();
				String ns = splittedDirective.next();
				while (splittedDirective.hasNext()) {
					String extension = splittedDirective.next();
					boolean isVersion = false;
					/*
					 * we have a ;
					 */
					Iterator<String> splittedExtension = Splitter.on('=')
							.trimResults(CharMatcher.is('"')).split(extension)
							.iterator();
					if (splittedExtension.hasNext()) {
						String instruction = splittedExtension.next();
						if ("version".equals(instruction)
								&& splittedExtension.hasNext()) {
							String versionValue = splittedExtension.next();
							if (versionValue != null) {
								manifestPackageVersion = versionValue;
								isVersion = true;
							}
						}
					}
					if (!isVersion) {
						extraExtensions.put(ns, extension);
					}
				}
				exportedPackages.put(ns, createVersion(manifestPackageVersion));
			}
		}
	}

	private Version createVersion(String manifestPackageVersion) {
		return Version.parse(manifestPackageVersion.replace(".qualifier",
				"-SNAPSHOT"));
	}

	public void setPackageVersion(String ns, Version inferedVersion) {
		if (exportedPackages.get(ns) != null) {
			exportedPackages.put(ns, inferedVersion);
		}
	}

	public void update(File manifestFile) throws IOException {
		if (exportedPackages.keySet().size() > 0) {
			String originalContent = Files.toString(manifestFile,
					Charsets.UTF_8);
			List<String> updatedContent = Lists.newArrayList();
			boolean isExportPackage = false;
			for (String part : Splitter.on(": ").split(originalContent)) {
				if (isExportPackage) {
					/*
					 * We retrieve the beginning of the last line which should
					 * be another directive.
					 */
					List<String> lines = Lists.newArrayList(Splitter.on("\n")
							.split(part));
					String startOfNextDirective = lines.get(lines.size() - 1);
					updatedContent.add(getExportPackageText() + "\n"
							+ startOfNextDirective);

				} else {
					updatedContent.add(part);
				}
				isExportPackage = part.endsWith("Export-Package");

			}
			String updatedFileContent = Joiner.on(": ").join(updatedContent);
			Files.write(updatedFileContent, manifestFile, Charsets.UTF_8);
		}
	}

	private String getExportPackageText() {
		String exportPackagesValues = "";
		if (exportedPackages.keySet().size() > 0) {
			List<String> exportedPackagesText = Lists.newArrayList();
			for (String ns : exportedPackages.keySet()) {
				String version = exportedPackages.get(ns).toString()
						.replace("-SNAPSHOT", ".qualifier");
				String extensions = ";version=\"" + version + "\"";
				List<String> allExtensions = Lists.newArrayList();
				allExtensions.add(extensions);
				allExtensions.addAll(extraExtensions.get(ns));
				exportedPackagesText.add(ns
						+ Joiner.on(';').join(allExtensions));
			}
			exportPackagesValues = Joiner.on(",\n ").join(exportedPackagesText);
		}
		return exportPackagesValues;
	}

}
