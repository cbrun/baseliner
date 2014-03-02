package fr.obeo.tools.osgi.baseliner;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;

import org.semver.Version;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

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
		exportedPackages.put(ns, inferedVersion);
	}

	public void update(File manifestFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(manifestFile);
		Manifest diskManifest = new Manifest(fileInputStream);

		List<String> exportedPackagesText = Lists.newArrayList();
		fileInputStream.close();
		if (exportedPackages.size() > 0) {
			for (Entry<String, Version> entry : exportedPackages.entrySet()) {
				String ns = entry.getKey();
				String version = entry.getValue().toString()
						.replace("-SNAPSHOT", ".qualifier");
				String extensions = ";version=\"" + version + "\"";
				List<String> allExtensions = Lists.newArrayList();
				allExtensions.add(extensions);
				allExtensions.addAll(extraExtensions.get(ns));
				exportedPackagesText.add(ns
						+ Joiner.on(';').join(allExtensions));
			}
			String exportPackagesValues = Joiner.on(",").join(
					exportedPackagesText);			
			diskManifest.getMainAttributes().putValue("Export-Package",
					exportPackagesValues);
			FileOutputStream out = new FileOutputStream(manifestFile);
			diskManifest.write(out);
			out.close();
		}
	}

}
