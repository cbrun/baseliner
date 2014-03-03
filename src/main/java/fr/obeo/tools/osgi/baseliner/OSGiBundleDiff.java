package fr.obeo.tools.osgi.baseliner;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osjava.jardiff.DiffCriteria;
import org.osjava.jardiff.DiffException;
import org.osjava.jardiff.JarDiff;
import org.semver.Delta;
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

public class OSGiBundleDiff {

	private JarDiff wrapped = new JarDiff();

	protected Map<String, Version> oldPackageVersions = Maps.newHashMap();

	protected Map<String, Version> newPackageVersions = Maps.newHashMap();

	public void loadNewClassesFromFolder(File folder) throws DiffException {
		if (folder.isDirectory()) {
			for (File child : folder.listFiles()) {
				String name = child.getName();
				if (!child.isDirectory() && name.endsWith(".class")) {
					wrapped.loadNewClasses(child);
				} else if (!child.isDirectory() && name.endsWith(".jar")) {
					wrapped.loadNewClasses(child);
					loadManifestVersionsFromJar(newPackageVersions, child);
				} else if (child.isDirectory()) {
					loadNewClassesFromFolder(child);
				}
			}
		}

	}

	public void loadOldClassesFromFolder(File folder) throws DiffException {
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
		}

	}

	private void loadManifestVersionsFromJar(Map<String, Version> packVersions,
			File child) throws DiffException {
		try {
			JarFile jar = new JarFile(child);
			Enumeration e = jar.entries();
			ManifestHandler handler = new ManifestHandler();
			while (e.hasMoreElements()) {
				JarEntry entry = (JarEntry) e.nextElement();
				String name = entry.getName();
				if (!entry.isDirectory() && "META-INF/MANIFEST.MF".equals(name)) {
					handler.load(jar.getInputStream(entry));
					packVersions.putAll(handler.getExportedPackages());
				}
			}
		} catch (IOException ioe) {
			throw new DiffException(ioe);
		}

	}

	public Map<String, Delta> diffByPackage(
			DifferenceAccumulatingHandler handler,
			DiffCriteria simpleDiffCriteria) throws DiffException {
		wrapped.diff(handler, simpleDiffCriteria);
		return splitByPackage(handler.getDelta());

	}

	private Map<String, Delta> splitByPackage(Delta in) {
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
			namespaceToDelta.put(ns,
					new Delta(Sets.newLinkedHashSet(namespaceToDiff.get(ns))));
		}
		return namespaceToDelta;
	}

	public Version getOldPackageVersion(String namespace) {
		return oldPackageVersions.get(namespace);
	}

	public Version getNewPackageVersion(String namespace) {
		return newPackageVersions.get(namespace);
	}

}
