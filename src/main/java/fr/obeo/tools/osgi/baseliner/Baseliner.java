package fr.obeo.tools.osgi.baseliner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.osjava.jardiff.DiffException;
import org.osjava.jardiff.SimpleDiffCriteria;
import org.semver.Delta;
import org.semver.Delta.Difference;
import org.semver.Dumper;
import org.semver.Version;
import org.semver.jardiff.DifferenceAccumulatingHandler;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Baseliner {

	private OSGiBundleDiff jDiff = new OSGiBundleDiff();
	/**
	 * The root folder of sources to update. These folders will be crawled for
	 * MANIFEST.MF files which are going to be updated based on the build
	 * deltas.
	 */
	@Option(name = "--src", handler = StringArrayOptionHandler.class, usage = "The root folder of sources to update. These folders will be crawled for MANIFEST.MF files which are going to be updated based on the build deltas.")
	private String[] srcLocations = { "/home/chipotortoose/Cedric/src/org.eclipse.emf.compare" };

	private Collection<String> newBuildsLocations = Lists
			.newArrayList("/home/chipotortoose/Cedric/Eclipse/4.3.2RC/eclipse/workspace/org.eclipse.pde.baselining/testdata/new/plugins");

	private Collection<String> oldBuildsLocations = Lists
			.newArrayList("/home/chipotortoose/Cedric/Eclipse/4.3.2RC/eclipse/workspace/org.eclipse.pde.baselining/testdata/old/plugins");

	public void updateExportPackageVersions() throws DiffException {

		for (String location : newBuildsLocations) {
			File jarLocation = new File(location);
			if (jarLocation.isDirectory() && jarLocation.exists()) {
				jDiff.loadNewClassesFromFolder(jarLocation);
			}
		}

		for (String location : oldBuildsLocations) {
			File jarLocation = new File(location);
			if (jarLocation.isDirectory() && jarLocation.exists()) {
				jDiff.loadOldClassesFromFolder(jarLocation);
			}
		}

		final DifferenceAccumulatingHandler handler = new DifferenceAccumulatingHandler();
		jDiff.diff(handler, new SimpleDiffCriteria());
		Map<String, Delta> result = splitByPackage(handler.getDelta());

		for (String srcRoot : srcLocations) {
			List<File> manifestFiles = findManifestFiles(new File(srcRoot));
			for (File manifestFile : manifestFiles) {
				try {
					FileInputStream fileInputStream = new FileInputStream(
							manifestFile);
					ManifestHandler manifestHandler = new ManifestHandler();
					manifestHandler.load(fileInputStream);

					for (Entry<String, Version> exportedPackage : manifestHandler
							.getExportedPackages().entrySet()) {

						String ns = exportedPackage.getKey();

						System.out
								.println("===================================");
						System.err.println("delta for " + ns);
						System.err.println("specified version for package is "
								+ exportedPackage.getValue());

						Delta packageDelta = result.get(ns);
						if (packageDelta != null) {
							if (jDiff.getOldPackageVersion(ns) != null) {
								System.out.println("old package version : "
										+ jDiff.getOldPackageVersion(ns));
								Version inferedVersion = packageDelta
										.infer(jDiff.getOldPackageVersion(ns));
								System.out.println("infered version :"
										+ inferedVersion);
								manifestHandler.setPackageVersion(ns,
										inferedVersion);
							} else {
								System.out
										.println("could not retrieve the old specified version.");
							}
						} else {
							System.out.println("no delta.");
						}
						// dumpNSCompat(result, ns);
					}

					manifestHandler.update(manifestFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void dumpNSCompat(Map<String, Delta> result, String ns) {
		if (result.get(ns) != null) {
			Delta.CompatibilityType expectedCompatibilityType = Delta.CompatibilityType
					.valueOf("BACKWARD_COMPATIBLE_USER");
			final Delta.CompatibilityType detectedCompatibilityType = result
					.get(ns).computeCompatibilityType();
			if (detectedCompatibilityType.compareTo(expectedCompatibilityType) > 0) {
				System.err.println("not backward compatible user");
				// Not compatible.
			}

			expectedCompatibilityType = Delta.CompatibilityType
					.valueOf("BACKWARD_COMPATIBLE_IMPLEMENTER");

			if (detectedCompatibilityType.compareTo(expectedCompatibilityType) > 0) {
				System.err.println("not backward compatible implementer");
				// Not compatible.
			}
			Dumper.dump(result.get(ns));
		}
	}

	private List<File> findManifestFiles(File file) {
		List<File> manifests = Lists.newArrayList();
		for (File child : file.listFiles()) {
			if ("MANIFEST.MF".equals(child.getName())) {
				manifests.add(child);
			}
			if (child.isDirectory()) {
				manifests.addAll(findManifestFiles(child));
			}
		}

		return manifests;
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

	public static void main(String[] args) {
		new Baseliner().doMain(args);
	}

	public void doMain(String[] args) {

		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.setUsageWidth(80);

		try {
			// parse the arguments.
			parser.parseArgument(args);

		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java SampleMain [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Example: java SampleMain"
					+ parser.printExample(OptionHandlerFilter.ALL));

			return;
		}

		try {
			updateExportPackageVersions();
		} catch (DiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
