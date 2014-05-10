package fr.obeo.baseliner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

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

	private String symbolicName;

	private Map<String, Version> exportedPackages = Maps.newLinkedHashMap();

	private Multimap<String, String> extraExtensions = HashMultimap.create();

	public ManifestHandler() {

	}

	public Map<String, Version> getExportedPackages() {
		return exportedPackages;
	}

	public void load(String in) throws IOException {
		InputStream is = new ByteArrayInputStream(in.getBytes());
		try {
			load(is);
		} finally {
			is.close();
		}

	}

	public void load(InputStream in) throws IOException {
		Headers<String, String> headers = new Headers<String, String>(10);
		Map<String, String> mapHeaders;
		try {
			mapHeaders = ManifestElement.parseBundleManifest(in, headers);
			bundleVersion = mapHeaders.get(Constants.BUNDLE_VERSION);
			symbolicName = mapHeaders.get(Constants.BUNDLE_SYMBOLICNAME);
			ManifestElement[] packages = ManifestElement.parseHeader(
					Constants.EXPORT_PACKAGE,
					mapHeaders.get(Constants.EXPORT_PACKAGE));
			if (packages != null) {
				for (ManifestElement manifestElement : packages) {
					String ns = manifestElement.getValue();
					String manifestPackageVersion = bundleVersion;
					String version = manifestElement
							.getAttribute(Constants.VERSION_ATTRIBUTE);
					if (version != null) {
						manifestPackageVersion = version;
					}

					Enumeration<String> attrKeys = manifestElement.getKeys();
					Enumeration<String> directiveKeys = manifestElement
							.getDirectiveKeys();

					StringBuffer result = new StringBuffer();
					if (attrKeys != null) {
						while (attrKeys.hasMoreElements()) {
							String key = attrKeys.nextElement();
							if (Constants.VERSION_ATTRIBUTE.equals(key)) {
								manifestPackageVersion = manifestElement
										.getAttribute(key);
							} else {
								result.append(addValues(false, key,
										manifestElement.getAttributes(key)));
							}
						}
					}
					if (directiveKeys != null) {
						while (directiveKeys.hasMoreElements()) {
							String key = directiveKeys.nextElement();
							result.append(addValues(true, key,
									manifestElement.getDirectives(key)));
						}
					}

					if (result.length() > 0) {
						extraExtensions.put(ns, result.toString());

					}

					exportedPackages.put(ns,
							createVersion(manifestPackageVersion));
				}
			}

		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String addValues(boolean directive, String key, String[] values) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			result.append(';').append(key);
			if (directive)
				result.append(':');
			result.append("=\"").append(values[i]).append('\"'); //$NON-NLS-1$			
		}
		return result.toString();
	}

	private Version createVersion(String manifestPackageVersion) {
		return new Version(manifestPackageVersion);
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
			String updatedFileContent = getMergedManifest(originalContent);
			if (!originalContent.equals(updatedFileContent)) {
				Files.write(updatedFileContent, manifestFile, Charsets.UTF_8);
			}
		}
	}

	public String getMergedManifest(String originalContent) {
		List<String> updatedContent = Lists.newArrayList();
		boolean isExportPackage = false;
		for (String part : Splitter.on(": ").split(originalContent)) {
			if (isExportPackage) {
				/*
				 * We retrieve the beginning of the last line which should be
				 * another directive.
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
		return updatedFileContent;
	}

	private String getExportPackageText() {
		String exportPackagesValues = "";
		if (exportedPackages.keySet().size() > 0) {
			List<String> exportedPackagesText = Lists.newArrayList();
			for (String ns : exportedPackages.keySet()) {
				String version = exportedPackages.get(ns).toString()
						.replace("-SNAPSHOT", ".qualifier");
				version = version.replace(".qualifier", "");
				String extensions = ";version=\"" + version + "\"";
				List<String> allExtensions = Lists.newArrayList();
				allExtensions.add(extensions);
				allExtensions.addAll(extraExtensions.get(ns));
				String extensionsText = Joiner.on("").join(allExtensions);
				exportedPackagesText.add(ns + extensionsText);
			}
			exportPackagesValues = Joiner.on(",\n ").join(exportedPackagesText);
		}
		return exportPackagesValues;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

}
