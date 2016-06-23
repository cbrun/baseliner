package fr.obeo.baseliner.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.target.WorkspaceFileTargetHandle;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class PDEInspector {

	private static final String TMP_PROJECT = "tmp-inspector";

	private static final String TP_FILENAME = "tp.target";

	private static final String TMP_TP_PATH = TMP_PROJECT + "/" + TP_FILENAME;

	private static final int TIMEOUT_IN_SEC = 30;

	private Optional<ITargetPlatformService> service;

	public PDEInspector() {
		service = Optional.fromNullable(
				(ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName()));
	}

	public void inspectProduct(File installationFolder, File reportsFolder, IProgressMonitor monitor)
			throws IOException {

		File extensionsFolder = new File(reportsFolder.getAbsolutePath() + File.separator + "extensions");

		if (installationFolder != null) {
			setInstallationAsTargetPlatform(installationFolder, monitor);
		}

		File pluginsFile = new File(reportsFolder.getAbsolutePath() + File.separator + "plugins");
		Files.createParentDirs(pluginsFile);
		FileWriter pluginsWritter = new FileWriter(pluginsFile);

		Multimap<String, String> idsToElements = LinkedHashMultimap.create();
		for (IPluginModelBase pdeModel : PluginRegistry.getActiveModels()) {
			try {
				File fileOrFolder = new File(pdeModel.getInstallLocation());
				String contentHash = "unknown";
				if (fileOrFolder.isFile()) {
					HashCode hashCode = com.google.common.io.Files.hash(fileOrFolder, Hashing.sha256());
					contentHash = hashCode.toString();
				}
				pluginsWritter.append("\n\n" + pdeModel.getBundleDescription().getSymbolicName() + "\n  content : "
						+ contentHash + "\n  version : " + pdeModel.getBundleDescription().getVersion());
				for (IPluginExtension ext : pdeModel.getExtensions().getExtensions()) {

					for (IPluginObject object : ext.getChildren()) {
						if (object instanceof IPluginElement) {
							IPluginElement element = (IPluginElement) object;
							String id = getAttributeValue(element, "id");
							String name = getAttributeValue(element, "name");
							StringBuffer out = new StringBuffer();
							out.append(
									"\n\n" + id + "  \n bundle : " + pdeModel.getBundleDescription().getSymbolicName());
							for (IPluginAttribute attr : element.getAttributes()) {
								if (!"id".equals(attr.getName())) {
									out.append("  \n " + attr.getName() + " : " + attr.getValue());
								}
							}
							idsToElements.put(ext.getPoint(), out.toString());

						}
					}

				}

			} catch (IOException e) {
				BaselinerAppPlugin.INSTANCE.log(new Status(IStatus.ERROR, BaselinerAppPlugin.INSTANCE.getSymbolicName(),
						"Error reading Jar file for" + pdeModel.getBundleDescription().getBundleId()
								+ " at the location " + pdeModel.getInstallLocation() + " .",
						e));
			}
		}
		pluginsWritter.flush();
		pluginsWritter.close();
		/*
		 * generate report files for extensions
		 */
		for (String extID : idsToElements.keySet()) {
			try {
				File to = new File(extensionsFolder.getAbsolutePath() + File.separator + extID);
				Files.createParentDirs(to);
				for (String line : Ordering.natural().immutableSortedCopy(idsToElements.get(extID))) {
					Files.append(line, to, Charsets.UTF_8);
				}
			} catch (IOException e) {
				BaselinerAppPlugin.INSTANCE.log(new Status(IStatus.ERROR, BaselinerAppPlugin.INSTANCE.getSymbolicName(),
						"Error saving extension points report " + extID + " .", e));
			}
		}

		/*
		 * generate report for features
		 */

		File featureFile = new File(reportsFolder.getAbsolutePath() + File.separator + "features");
		Files.createParentDirs(featureFile);
		FileWriter featuresWriter = new FileWriter(
				featureFile);

		for (IFeatureModel feature : PDECore.getDefault().getFeatureModelManager().getModels()) {
			File fileOrFolder = new File(feature.getInstallLocation());
			String contentHash = "unknown";
			if (fileOrFolder.isFile()) {
				HashCode hashCode = com.google.common.io.Files.hash(fileOrFolder, Hashing.sha256());
				contentHash = hashCode.toString();
			}
			featuresWriter.append("\n\n" + feature.getFeature().getId() + "\n  content : " + contentHash
					+ "\n  version : " + feature.getFeature().getVersion());
		}

		featuresWriter.flush();
		featuresWriter.close();

	}

	private String getAttributeValue(IPluginElement element, String key) {
		IPluginAttribute attr = element.getAttribute(key);
		if (attr != null) {
			return attr.getValue();
		}
		return null;
	}

	private void setInstallationAsTargetPlatform(File installationFolder, IProgressMonitor monitor) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (root != null) {
				IProject prj = root.getProject(TMP_PROJECT);
				if (!prj.exists()) {
					prj.create(monitor);
				}
				prj.open(monitor);

				String fileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
						+ "<?pde version=\"3.8\"?><target name=\"install\" sequenceNumber=\"0\">\n" + "<locations>\n"
						+ "<location path=\"" + installationFolder.getAbsolutePath() + "\" type=\"Directory\"/>\n"
						+ "</locations>\n" + "</target>\n" + "";

				prj.getFile(TP_FILENAME).create(new ByteArrayInputStream(fileContent.getBytes("UTF-8")), true, monitor);

				setTargetPlatform(monitor);

				int secondsWaiting = 0;
				while (!Job.getJobManager().isIdle() && secondsWaiting <= TIMEOUT_IN_SEC) {
					Job currentJob = Job.getJobManager().currentJob();
					String jobName = "unknown";
					ISchedulingRule currentrule = Job.getJobManager().currentRule();
					if (currentrule != null) {
						jobName = currentrule.toString();
					}
					if (currentJob != null) {
						jobName = currentJob.getName();
						if (jobName == null) {
							jobName = currentJob.getClass().getCanonicalName();
						}
					}
					Thread.sleep(1000);
					secondsWaiting++;
				}

			}
		} catch (CoreException e) {
			BaselinerAppPlugin.INSTANCE.log(new Status(IStatus.ERROR, BaselinerAppPlugin.INSTANCE.getSymbolicName(),
					"Error setting folder as target platfrorm" + installationFolder.getAbsolutePath() + " .", e));
		} catch (UnsupportedEncodingException e) {
			BaselinerAppPlugin.INSTANCE.log(new Status(IStatus.ERROR, BaselinerAppPlugin.INSTANCE.getSymbolicName(),
					"Error setting folder as target platfrorm" + installationFolder.getAbsolutePath() + " .", e));
		} catch (InterruptedException e) {
			BaselinerAppPlugin.INSTANCE.log(new Status(IStatus.ERROR, BaselinerAppPlugin.INSTANCE.getSymbolicName(),
					"Error setting folder as target platfrorm" + installationFolder.getAbsolutePath() + " .", e));
		}
	}

	public void setTargetPlatform(IProgressMonitor monitor) throws CoreException {
		if (service.isPresent()) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
			ITargetHandle[] targets = service.get().getTargets(subMonitor.newChild(20));

			boolean hasBeenSet = false;
			for (int i = 0; i < targets.length && !hasBeenSet; i++) {
				if (targets[i] instanceof WorkspaceFileTargetHandle) {
					WorkspaceFileTargetHandle fileHandle = (WorkspaceFileTargetHandle) targets[i];
					if (TMP_TP_PATH.equals(fileHandle.getTargetFile().getProject().getName() + "/"
							+ fileHandle.getTargetFile().getProjectRelativePath().toPortableString())) {
						hasBeenSet = trySetTargetPlatform(subMonitor, fileHandle);
					}
				}
			}
		}
	}

	private boolean trySetTargetPlatform(SubMonitor subMonitor, WorkspaceFileTargetHandle fileHandle)
			throws CoreException {
		File targetFile = ((WorkspaceFileTargetHandle) fileHandle).getTargetFile().getLocation().toFile();
		ITargetDefinition targetDefinitionToSet = fileHandle.getTargetDefinition();

		if (!targetDefinitionToSet.isResolved()) {
			targetDefinitionToSet.resolve(subMonitor.newChild(80));
		}
		subMonitor.setWorkRemaining(0);

		IStatus compare = service.get().compareWithTargetPlatform(targetDefinitionToSet);
		if (compare != null && !compare.isOK()) {
			org.eclipse.pde.core.target.LoadTargetDefinitionJob.load(targetDefinitionToSet);
		}
		return true;
	}

}