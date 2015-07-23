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

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

public class ManifestRewriter {

	private String bundleVersion;

	private Optional<Version> newBundleVersion = Optional.absent();

	private String symbolicName;

	private Map<String, Version> exportedPackages = Maps.newLinkedHashMap();

	private Map<String, Boolean> versionIsFirst = Maps.newLinkedHashMap();

	private Multimap<String, String> extraExtensions = LinkedHashMultimap.create();

	public ManifestRewriter() {

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
			if (symbolicName.indexOf(";") != -1) {
				symbolicName = symbolicName.substring(0, symbolicName.indexOf(";"));
			}
			ManifestElement[] packages = ManifestElement.parseHeader(Constants.EXPORT_PACKAGE,
					mapHeaders.get(Constants.EXPORT_PACKAGE));
			if (packages != null) {
				for (ManifestElement manifestElement : packages) {
					String ns = manifestElement.getValue();
					String manifestPackageVersion = bundleVersion;
					String version = manifestElement.getAttribute(Constants.VERSION_ATTRIBUTE);
					if (version != null) {
						manifestPackageVersion = version;
					}

					Enumeration<String> attrKeys = manifestElement.getKeys();
					Enumeration<String> directiveKeys = manifestElement.getDirectiveKeys();

					StringBuffer result = new StringBuffer();
					int nbElement = 0;
					if (directiveKeys != null) {
						while (directiveKeys.hasMoreElements()) {
							String key = directiveKeys.nextElement();
							result.append(addValues(true, key, manifestElement.getDirectives(key)));
							nbElement++;
						}
					}
					if (attrKeys != null) {
						while (attrKeys.hasMoreElements()) {
							String key = attrKeys.nextElement();
							if (Constants.VERSION_ATTRIBUTE.equals(key)) {
								versionIsFirst.put(ns, nbElement == 0);
								manifestPackageVersion = manifestElement.getAttribute(key);
							} else {
								result.append(addValues(false, key, manifestElement.getAttributes(key)));
							}
							nbElement++;
						}
					}

					if (result.length() > 0) {
						extraExtensions.put(ns, result.toString());

					}

					exportedPackages.put(ns, createVersion(manifestPackageVersion));
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
			String value = values[i];
			if ((key.equals("uses") | key.equals("x-friends")) && value.contains(",")) {
				result.append(";\n  ").append(key);
			} else {
				result.append(';').append(key);
			}
			if (directive)
				result.append(':');
			value = value.replace(",  ", ",\n  ");
			if (value.contains(",")) {
				result.append("=\"").append(value).append('\"'); //$NON-NLS-1$			
			} else {
				result.append("=").append(value); //$NON-NLS-1$
			}
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

	public void setNewBundleVersion(Version newVersion) {
		this.newBundleVersion = Optional.fromNullable(newVersion);
	}

	public Optional<String> update(File manifestFile) throws IOException {
		if (exportedPackages.keySet().size() > 0) {
			String originalContent = Files.toString(manifestFile, Charsets.UTF_8);
			String updatedFileContent = getMergedManifest(originalContent);
			if (!originalContent.equals(updatedFileContent)) {
				Files.write(updatedFileContent, manifestFile, Charsets.UTF_8);
				return Optional.of(updatedFileContent);
			}
			return Optional.of(originalContent);
		}
		return Optional.absent();
	}

	public String getMergedManifest(String originalContent) {
		List<String> updatedContent = Lists.newArrayList();
		boolean isExportPackage = false;
		boolean isBundleVersion = false;
		for (String part : Splitter.on(": ").split(originalContent)) {
			if (isBundleVersion) {
				/*
				 * this logic should not be in the manifest handler.
				 */
				String newVersion = getNewBundleVersion();

				List<String> lines = Lists.newArrayList(Splitter.on("\n").split(part));
				String startOfNextDirective = lines.get(lines.size() - 1);
				updatedContent.add(newVersion + "\n" + startOfNextDirective);
			} else if (isExportPackage) {
				/*
				 * We retrieve the beginning of the last line which should be
				 * another directive.
				 */
				List<String> lines = Lists.newArrayList(Splitter.on("\n").split(part));
				String startOfNextDirective = lines.get(lines.size() - 1);
				updatedContent.add(getExportPackageText() + "\n" + startOfNextDirective);

			} else {
				updatedContent.add(part);
			}
			isExportPackage = part.endsWith("Export-Package");
			isBundleVersion = part.endsWith("Bundle-Version");

		}
		String updatedFileContent = Joiner.on(": ").join(updatedContent);
		return updatedFileContent;
	}

	private String getNewBundleVersion() {
		String newVersion = bundleVersion;
		if (newBundleVersion.isPresent()) {
			newVersion = newBundleVersion.get().toString();
		}

		if (bundleVersion.endsWith(".qualifier") && !newVersion.endsWith(".qualifier")) {
			newVersion = newVersion + ".qualifier";
		}
		return newVersion;
	}

	public Version getHighestExportedVersion() {
		Version highestVersion = new Version(bundleVersion);
		for (String ns : exportedPackages.keySet()) {
			/*
			 * strictly speaking that means we agree in creating "broken"
			 * situations because from an OSGi point of view x:internal does not
			 * mean much. That being said, if we consider Import-Package is the
			 * way to go, then the consummer will effectively see the break.
			 */
			if (!isMarkedInternal(ns)) {
				String versionText = exportedPackages.get(ns).toString().replace("-SNAPSHOT", ".qualifier");
				Version packageVersion = new Version(0, 0, 0);
				if (versionText != null) {
					packageVersion = new Version(versionText);
				}
				if (packageVersion.compareTo(highestVersion) > 0) {
					highestVersion = packageVersion;
				}
			}
		}
		if (this.bundleVersion != null && this.bundleVersion.endsWith(".qualifier")
				&& highestVersion.getQualifier().length() == 0) {
			highestVersion = new Version(highestVersion.toString() + ".qualifier");
		}
		return highestVersion;
	}

	public boolean isMarkedInternal(String ns) {
		for (String extraExtension : extraExtensions.get(ns)) {
			if (extraExtension != null && extraExtension.contains("x-internal:=")) {
				return true;
			}
		}
		return false;
	}

	private String getExportPackageText() {
		String exportPackagesValues = "";
		if (exportedPackages.keySet().size() > 0) {
			List<String> exportedPackagesText = Lists.newArrayList();
			for (String ns : exportedPackages.keySet()) {
				String version = exportedPackages.get(ns).toString().replace("-SNAPSHOT", ".qualifier");
				version = version.replace(".qualifier", "");
				String extensions = ";version=\"" + version + "\"";
				List<String> allExtensions = Lists.newArrayList();
				if (versionIsFirstInLine(ns)) {
					allExtensions.add(extensions);
					allExtensions.addAll(extraExtensions.get(ns));
				} else {
					allExtensions.addAll(extraExtensions.get(ns));
					allExtensions.add(extensions);
				}
				String extensionsText = Joiner.on("").join(allExtensions);
				exportedPackagesText.add(ns + extensionsText);
			}
			exportPackagesValues = Joiner.on(",\n ").join(exportedPackagesText);
		}
		return exportPackagesValues;
	}

	private boolean versionIsFirstInLine(String ns) {
		Boolean versionWasFirst = versionIsFirst.get(ns);
		if (versionWasFirst != null) {
			return versionWasFirst.booleanValue();
		}
		return false;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

}
