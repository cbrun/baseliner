package fr.obeo.baseliner.tests.unit;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.junit.Test;

import fr.obeo.baseliner.ManifestHandler;

public class TestManifestHandler {

	@Test
	public void testCompare3() throws IOException {
		String compareManifest = "Manifest-Version: 1.0\n"
				+ "Bundle-ManifestVersion: 2\n"
				+ "Bundle-Name: %pluginName\n"
				+ "Bundle-SymbolicName: org.eclipse.emf.compare\n"
				+ "Bundle-Version: 3.0.0.qualifier\n"
				+ "Bundle-ClassPath: .\n"
				+ "Bundle-Vendor: %providerName\n"
				+ "Bundle-Localization: plugin\n"
				+ "Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"
				+ "Export-Package: org.eclipse.emf.compare,\n"
				+ " org.eclipse.emf.compare.conflict,\n"
				+ " org.eclipse.emf.compare.diff,\n"
				+ " org.eclipse.emf.compare.equi,\n"
				+ " org.eclipse.emf.compare.impl,\n"
				+ " org.eclipse.emf.compare.internal;x-friends:=\"org.eclipse.emf.compare.logical,org.eclipse.emf.compare.ide\",\n"
				+ " org.eclipse.emf.compare.internal.postprocessor.factories;x-internal:=true,\n"
				+ " org.eclipse.emf.compare.internal.spec;x-friends:=\"org.eclipse.emf.compare.tests\",\n"
				+ " org.eclipse.emf.compare.match,\n"
				+ " org.eclipse.emf.compare.match.eobject,\n"
				+ " org.eclipse.emf.compare.match.eobject.internal;x-internal:=true,\n"
				+ " org.eclipse.emf.compare.match.impl,\n"
				+ " org.eclipse.emf.compare.match.resource,\n"
				+ " org.eclipse.emf.compare.merge,\n"
				+ " org.eclipse.emf.compare.postprocessor,\n"
				+ " org.eclipse.emf.compare.req,\n"
				+ " org.eclipse.emf.compare.scope,\n"
				+ " org.eclipse.emf.compare.util,\n"
				+ " org.eclipse.emf.compare.utils\n"
				+ "Require-Bundle: org.eclipse.emf.ecore;visibility:=reexport,\n"
				+ " org.eclipse.emf.ecore.xmi;bundle-version=\"2.5.0\"\n"
				+ "Bundle-ActivationPolicy: lazy\n"
				+ "Import-Package: com.google.common.base;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.cache;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.collect;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.util.concurrent;version=\"[11.0.0,15.0.0)\"\n"
				+ "";
		String expected = "Manifest-Version: 1.0\n"
				+ "Bundle-ManifestVersion: 2\n"
				+ "Bundle-Name: %pluginName\n"
				+ "Bundle-SymbolicName: org.eclipse.emf.compare\n"
				+ "Bundle-Version: 3.0.0.qualifier\n"
				+ "Bundle-ClassPath: .\n"
				+ "Bundle-Vendor: %providerName\n"
				+ "Bundle-Localization: plugin\n"
				+ "Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"
				+ "Export-Package: org.eclipse.emf.compare;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.conflict;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.diff;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.equi;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.impl;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.internal;version=\"3.0.0\";x-friends:=\"org.eclipse.emf.compare.logical,org.eclipse.emf.compare.ide\",\n"
				+ " org.eclipse.emf.compare.internal.postprocessor.factories;version=\"3.0.0\";x-internal:=\"true\",\n"
				+ " org.eclipse.emf.compare.internal.spec;version=\"3.0.0\";x-friends:=\"org.eclipse.emf.compare.tests\",\n"
				+ " org.eclipse.emf.compare.match;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.match.eobject;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.match.eobject.internal;version=\"3.0.0\";x-internal:=\"true\",\n"
				+ " org.eclipse.emf.compare.match.impl;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.match.resource;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.merge;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.postprocessor;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.req;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.scope;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.util;version=\"3.0.0\",\n"
				+ " org.eclipse.emf.compare.utils;version=\"3.0.0\"\n"
				+ "Require-Bundle: org.eclipse.emf.ecore;visibility:=reexport,\n"
				+ " org.eclipse.emf.ecore.xmi;bundle-version=\"2.5.0\"\n"
				+ "Bundle-ActivationPolicy: lazy\n"
				+ "Import-Package: com.google.common.base;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.cache;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.collect;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.util.concurrent;version=\"[11.0.0,15.0.0)\"\n";
		compareWithExpected(compareManifest, expected);

	}

	@Test
	public void testCompare4() throws IOException {
		String compareManifest = "Manifest-Version: 1.0\n"
				+ "Bundle-ManifestVersion: 2\n"
				+ "Bundle-Name: %pluginName\n"
				+ "Bundle-SymbolicName: org.eclipse.emf.compare\n"
				+ "Bundle-Version: 3.1.0.qualifier\n"
				+ "Bundle-ClassPath: .\n"
				+ "Bundle-Vendor: %providerName\n"
				+ "Bundle-Localization: plugin\n"
				+ "Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"
				+ "Export-Package: org.eclipse.emf.compare,\n"
				+ " org.eclipse.emf.compare.conflict,\n"
				+ " org.eclipse.emf.compare.diff,\n"
				+ " org.eclipse.emf.compare.equi,\n"
				+ " org.eclipse.emf.compare.impl,\n"
				+ " org.eclipse.emf.compare.internal;x-friends:=\"org.eclipse.emf.compare.logical,org.eclipse.emf.compare.ide\",\n"
				+ " org.eclipse.emf.compare.internal.merge;x-friends:=\"org.eclipse.emf.compare.rcp.ui,org.eclipse.emf.compare.edit,org.eclipse.emf.compare.ide.ui\",\n"
				+ " org.eclipse.emf.compare.internal.postprocessor.factories;x-internal:=true,\n"
				+ " org.eclipse.emf.compare.internal.spec;x-friends:=\"org.eclipse.emf.compare.tests,org.eclipse.emf.compare.rcp.ui\",\n"
				+ " org.eclipse.emf.compare.internal.utils;\n"
				+ "  x-friends:=\"org.eclipse.emf.compare.ide.ui,\n"
				+ "   org.eclipse.emf.compare.tests,\n"
				+ "   org.eclipse.emf.compare.uml2,\n"
				+ "   org.eclipse.emf.compare.diagram.ide.ui,\n"
				+ "   org.eclipse.emf.compare.rcp.ui\",\n"
				+ " org.eclipse.emf.compare.match,\n"
				+ " org.eclipse.emf.compare.match.eobject,\n"
				+ " org.eclipse.emf.compare.match.eobject.internal;x-friends:=\"org.eclipse.emf.compare.tests\",\n"
				+ " org.eclipse.emf.compare.match.impl,\n"
				+ " org.eclipse.emf.compare.match.resource,\n"
				+ " org.eclipse.emf.compare.merge,\n"
				+ " org.eclipse.emf.compare.postprocessor,\n"
				+ " org.eclipse.emf.compare.req,\n"
				+ " org.eclipse.emf.compare.scope,\n"
				+ " org.eclipse.emf.compare.util,\n"
				+ " org.eclipse.emf.compare.utils\n"
				+ "Require-Bundle: org.eclipse.emf.ecore;visibility:=reexport,\n"
				+ " org.eclipse.emf.ecore.xmi;bundle-version=\"2.5.0\"\n"
				+ "Bundle-ActivationPolicy: lazy\n"
				+ "Import-Package: com.google.common.base;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.cache;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.collect;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.util.concurrent;version=\"[11.0.0,15.0.0)\"\n"
				+ "";
		String expected = "Manifest-Version: 1.0\n"
				+ "Bundle-ManifestVersion: 2\n"
				+ "Bundle-Name: %pluginName\n"
				+ "Bundle-SymbolicName: org.eclipse.emf.compare\n"
				+ "Bundle-Version: 3.1.0.qualifier\n"
				+ "Bundle-ClassPath: .\n"
				+ "Bundle-Vendor: %providerName\n"
				+ "Bundle-Localization: plugin\n"
				+ "Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"
				+ "Export-Package: org.eclipse.emf.compare;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.conflict;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.diff;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.equi;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.impl;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.internal;version=\"3.1.0\";x-friends:=\"org.eclipse.emf.compare.logical,org.eclipse.emf.compare.ide\",\n"
				+ " org.eclipse.emf.compare.internal.merge;version=\"3.1.0\";x-friends:=\"org.eclipse.emf.compare.rcp.ui,org.eclipse.emf.compare.edit,org.eclipse.emf.compare.ide.ui\",\n"
				+ " org.eclipse.emf.compare.internal.postprocessor.factories;version=\"3.1.0\";x-internal:=\"true\",\n"
				+ " org.eclipse.emf.compare.internal.spec;version=\"3.1.0\";x-friends:=\"org.eclipse.emf.compare.tests,org.eclipse.emf.compare.rcp.ui\",\n"
				+ " org.eclipse.emf.compare.internal.utils;version=\"3.1.0\";x-friends:=\"org.eclipse.emf.compare.ide.ui,  org.eclipse.emf.compare.tests,  org.eclipse.emf.compare.uml2,  org.eclipse.emf.compare.diagram.ide.ui,  org.eclipse.emf.compare.rcp.ui\",\n"
				+ " org.eclipse.emf.compare.match;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.match.eobject;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.match.eobject.internal;version=\"3.1.0\";x-friends:=\"org.eclipse.emf.compare.tests\",\n"
				+ " org.eclipse.emf.compare.match.impl;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.match.resource;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.merge;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.postprocessor;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.req;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.scope;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.util;version=\"3.1.0\",\n"
				+ " org.eclipse.emf.compare.utils;version=\"3.1.0\"\n"
				+ "Require-Bundle: org.eclipse.emf.ecore;visibility:=reexport,\n"
				+ " org.eclipse.emf.ecore.xmi;bundle-version=\"2.5.0\"\n"
				+ "Bundle-ActivationPolicy: lazy\n"
				+ "Import-Package: com.google.common.base;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.cache;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.collect;version=\"[11.0.0,15.0.0)\",\n"
				+ " com.google.common.util.concurrent;version=\"[11.0.0,15.0.0)\"\n"
				+ "";
		compareWithExpected(compareManifest, expected);

	}

	private void compareWithExpected(String compareManifest, String expected)
			throws IOException {
		ManifestHandler handler = new ManifestHandler();
		handler.load(compareManifest);
		String merged = handler.getMergedManifest(compareManifest);
		loadAsJavaManifest(merged);
		assertEquals(expected, merged);
	}

	private void loadAsJavaManifest(String result) throws IOException {
		InputStream is = new ByteArrayInputStream(result.getBytes());
		try {
			Manifest reloaded = new Manifest(is);

		} finally {
			is.close();
		}
	}

}
