package fr.obeo.baseliner.pde;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.target.WorkspaceFileTargetHandle;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import fr.obeo.baseliner.BaselinerJarProvider;

public class PDEBaselineJarProvider implements BaselinerJarProvider {

	private Map<String, ITargetDefinition> platformPathToTargetDefinition = Maps.newHashMap();

	private Map<String, HashCode> platformPathToPreviousHashcode = Maps.newHashMap();

	private static HashFunction hasher = Hashing.goodFastHash(14);

	@Override
	public File getPreviousJar(String catalogURI, String symbolicName, String bundleVersion, IProgressMonitor monitor,
			Collection<IStatus> statuses) {
		if (catalogURI != null && !monitor.isCanceled()) {
			if (catalogURI.equals("platform:/pde/apibaselines")) {
				for (IApiBaseline baseline : ApiPlugin.getDefault().getApiBaselineManager().getApiBaselines()) {
					IApiComponent bundle = baseline.getApiComponent(symbolicName);
					if (bundle != null && bundle.getLocation() != null) {
						File jarFile = new File(bundle.getLocation());
						if (jarFile.exists()) {
							return jarFile;
						}
					}

				}
			} else if (catalogURI.startsWith("platform:/resource") && catalogURI.endsWith(".target")) {
				String pathInWorkspace = catalogURI.substring("platform:/resource".length());
				IResource tpFile = ResourcesPlugin.getWorkspace().getRoot().findMember(pathInWorkspace);
				File found = null;
				if (tpFile instanceof IFile) {
					if (tpFile.exists() && tpFile.isAccessible()) {
						IFile file = (IFile) tpFile;
						File tpIOFile = new File(file.getLocation().toOSString());
						if (tpIOFile.exists() && tpIOFile.canRead()) {
							try {
								HashCode currentTPFileHashCode = Files.hash(tpIOFile, hasher);
								ITargetDefinition def = platformPathToTargetDefinition.get(catalogURI);
								if (def == null
										|| !platformPathToPreviousHashcode.get(catalogURI)
												.equals(currentTPFileHashCode)) {
									WorkspaceFileTargetHandle myHandle = new WorkspaceFileTargetHandle(file);
									def = myHandle.getTargetDefinition();
									IStatus resolveStatus = def.resolve(monitor);
									if (!resolveStatus.isOK()) {
										statuses.add(resolveStatus);
									}
									platformPathToTargetDefinition.put(catalogURI, def);
									platformPathToPreviousHashcode.put(catalogURI, currentTPFileHashCode);
								} else {
									def = platformPathToTargetDefinition.get(catalogURI);
								}

								/*
								 * at this point we should have a tp definition
								 * which is up-to-date based on the file
								 * content.
								 */
								return searchFileFromBundleInfo(def, symbolicName, monitor);

							} catch (IOException e) {
								statuses.add(error("Error reading the target definition file :" + tpIOFile.getPath(), e));
							} catch (CoreException e) {
								statuses.add(error(
										"Error resolving the target definition file :" + tpFile.getFullPath(), e));
							}

						} else {
							statuses.add(error("Error reading the target definition file :" + tpIOFile.getPath()));
						}
					} else {
						statuses.add(error("Error reading the target definition file :" + pathInWorkspace));
					}

				} else {
					statuses.add(error("Error reading the target definition file :" + pathInWorkspace));
				}

			}
		}

		return null;
	}

	private IStatus error(String message) {
		return new Status(IStatus.ERROR, "fr.obeo.baseliner.pde", message);
	}

	private IStatus error(String message, Throwable e) {
		return new Status(IStatus.ERROR, "fr.obeo.baseliner.pde", message, e);
	}

	private File searchFileFromBundleInfo(ITargetDefinition def, String bundleSymbolicName, IProgressMonitor monitor)
			throws CoreException {
		File found = null;
		for (TargetBundle bundle : def.getAllBundles()) {
			if (bundleSymbolicName.equals(bundle.getBundleInfo().getSymbolicName())) {
				URI bundlePhysicalLocation = bundle.getBundleInfo().getLocation();
				if (bundlePhysicalLocation != null) {
					found = new File(bundlePhysicalLocation.getPath());
					break;
				}
			}
		}
		return found;
	}
}
