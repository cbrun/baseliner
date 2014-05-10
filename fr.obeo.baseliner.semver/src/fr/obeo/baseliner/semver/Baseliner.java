package fr.obeo.baseliner.semver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.osgi.framework.Version;
import org.osjava.jardiff.DiffException;

import com.google.common.collect.Lists;

import fr.obeo.baseliner.ApiComparator;
import fr.obeo.baseliner.Delta;
import fr.obeo.baseliner.ManifestHandler;

public class Baseliner {

	private ApiComparator jDiff = new OSGiBundleDiff();//
	/**
	 * The root folder of sources to update. These folders will be crawled for
	 * MANIFEST.MF files which are going to be updated based on the build
	 * deltas.
	 */
	@Option(name = "--src", handler = StringArrayOptionHandler.class, usage = "The root folder of sources to update. These folders will be crawled for MANIFEST.MF files which are going to be updated based on the build deltas.")
	private String[] srcLocations = {};

	@Option(name = "--newbin", handler = StringArrayOptionHandler.class, usage = "Root folders to look for the 'new' binaries. It can contains both .jar or .class files and will be recursively crawled.")
	private String[] newBuildsLocations = {};

	@Option(name = "--oldbin", handler = StringArrayOptionHandler.class, usage = "Root folders to look for the 'old' or baseline binaries. It can contains both .jar or .class files and will be recursively crawled.")
	private String[] oldBuildsLocations = {};

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

		Map<String, Delta> result = jDiff.diffByPackage();

		for (String srcRoot : srcLocations) {
			List<File> manifestFiles = findManifestFiles(new File(srcRoot));
			for (File manifestFile : manifestFiles) {
				try (FileInputStream fileInputStream = new FileInputStream(
						manifestFile);) {
					ManifestHandler manifestHandler = new ManifestHandler();
					manifestHandler.load(fileInputStream);

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
							if (jDiff.getOldPackageVersion(ns) != null) {
								Version inferedVersion = packageDelta
										.infer(jDiff.getOldPackageVersion(ns));
								if (inferedVersion.compareTo(jDiff
										.getOldPackageVersion(ns)) != 0) {
									System.out.println("==== " + ns
											+ " package version : "
											+ jDiff.getOldPackageVersion(ns)
											+ " but infered version is :"
											+ inferedVersion);
//									dumpNSCompat(result.get(ns));
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

//	private void dumpNSCompat(Delta delta) {
//
//		Delta.CompatibilityType expectedCompatibilityType = Delta.CompatibilityType
//				.valueOf("BACKWARD_COMPATIBLE_USER");
//		final Delta.CompatibilityType detectedCompatibilityType = delta
//				.computeCompatibilityType();
//		if (detectedCompatibilityType.compareTo(expectedCompatibilityType) > 0) {
//			System.err.println("/!\\ not backward compatible user");
//			// Not compatible.
//		}
//
//		expectedCompatibilityType = Delta.CompatibilityType
//				.valueOf("BACKWARD_COMPATIBLE_IMPLEMENTER");
//
//		if (detectedCompatibilityType.compareTo(expectedCompatibilityType) > 0) {
//			System.err.println("/!\\ not backward compatible implementer");
//			// Not compatible.
//		}
//		Dumper.dump(delta);
//	}

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
			//System.err.println("  Example: java SampleMain"
			//		+ parser.printExample(OptionHandlerFilter.ALL));

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
