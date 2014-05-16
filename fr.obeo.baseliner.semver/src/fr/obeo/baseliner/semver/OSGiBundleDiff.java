package fr.obeo.baseliner.semver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osjava.jardiff.DiffCriteria;
import org.osjava.jardiff.DiffException;
import org.osjava.jardiff.ExtendedJarDiff;
import org.osjava.jardiff.SimpleDiffCriteria;
import org.semver.Delta.Difference;
import org.semver.Version;
import org.semver.jardiff.DifferenceAccumulatingHandler;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.obeo.baseliner.ApiComparator;
import fr.obeo.baseliner.Delta;
import fr.obeo.baseliner.ManifestHandler;

public class OSGiBundleDiff implements ApiComparator {

	private ExtendedJarDiff wrapped = new ExtendedJarDiff();

	protected Map<String, Version> oldPackageVersions = Maps.newHashMap();

	protected Map<String, Version> newPackageVersions = Maps.newHashMap();

	public void loadNewClassesFromFolder(File projectFolder, File folder)
			throws RuntimeException {
		try {
			if (folder.isDirectory()) {
				for (File child : folder.listFiles()) {
					String name = child.getName();
					if (!child.isDirectory() && name.endsWith(".class")) {
						wrapped.loadNewClassFile(child);
					} else if (!child.isDirectory() && name.endsWith(".jar")) {
						wrapped.loadNewClasses(child);
						loadManifestVersionsFromJar(newPackageVersions, child);
					} else if (child.isDirectory()) {
						loadNewClassesFromFolder(projectFolder, child);
					}
				}
			}
		} catch (DiffException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void loadOldClassesFromFolder(File folder) throws RuntimeException {
		try {
			if (folder.isDirectory()) {
				for (File child : folder.listFiles()) {
					String name = child.getName();
					if (!child.isDirectory() && name.endsWith(".class")) {
						wrapped.loadOldClasses(child);
					} else if (!child.isDirectory() && name.endsWith(".jar")) {
						wrapped.loadOldClasses(child);
						loadManifestVersionsFromJar(oldPackageVersions, child);
					} else if (child.isDirectory()) {
						loadOldClassesFromFolder(child);
					}
				}
			} else if (folder.getName() != null
					&& folder.getName().endsWith("jar")) {
				wrapped.loadOldClasses(folder);
				loadManifestVersionsFromJar(oldPackageVersions, folder);
			}

		} catch (DiffException e) {
			throw new RuntimeException(e);
		}

	}

	private void loadManifestVersionsFromJar(Map<String, Version> packVersions,
			File child) throws RuntimeException {
		try {
			JarFile jar = new JarFile(child);
			try {
				Enumeration<JarEntry> e = jar.entries();
				ManifestHandler handler = new ManifestHandler();
				while (e.hasMoreElements()) {
					JarEntry entry = (JarEntry) e.nextElement();
					String name = entry.getName();
					if (!entry.isDirectory()
							&& "META-INF/MANIFEST.MF".equals(name)) {
						InputStream in = jar.getInputStream(entry);
						try {
							handler.load(in);
							for (Entry<String, org.osgi.framework.Version> mapEntry : handler
									.getExportedPackages().entrySet()) {
								packVersions.put(mapEntry.getKey(),
										WrappedDelta.toSemVersion(mapEntry
												.getValue()));
							}
						} finally {
							in.close();
						}
					}
				}
			} finally {
				jar.close();
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	public Map<String, Delta> diffByPackage(
			DifferenceAccumulatingHandler handler,
			DiffCriteria simpleDiffCriteria) throws RuntimeException {
		try {
			wrapped.diff(handler, simpleDiffCriteria);
		} catch (DiffException e) {
			throw new RuntimeException(e);
		}
		return splitByPackage(handler.getDelta());

	}

	private Map<String, Delta> splitByPackage(org.semver.Delta in) {
		Multimap<String, Difference> namespaceToDiff = HashMultimap.create();
		Splitter dotSplit = Splitter.on('.');
		for (Difference diff : in.getDifferences()) {
			List<String> trimmed = Lists.newArrayList();
			Iterable<String> segments = dotSplit.split(diff.getClassName());
			Iterator<String> segIterator = segments.iterator();
			while (segIterator.hasNext()) {
				String cur = segIterator.next();
				if (segIterator.hasNext()) {
					trimmed.add(cur);
				}
			}
			String namespace = Joiner.on('.').join(trimmed);
			namespaceToDiff.put(namespace, diff);
		}
		Map<String, Delta> namespaceToDelta = Maps.newHashMap();
		for (String ns : namespaceToDiff.keySet()) {
			namespaceToDelta.put(
					ns,
					new WrappedDelta(new org.semver.Delta(Sets
							.newLinkedHashSet(namespaceToDiff.get(ns))),
							oldPackageVersions.get(ns)));
		}
		return namespaceToDelta;
	}

	@Override
	public Map<String, Delta> diffByPackage() {
		final DifferenceAccumulatingHandler diffHandler = new DifferenceAccumulatingHandler();
		return diffByPackage(diffHandler, new SimpleDiffCriteria());
	}

}
