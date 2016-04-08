package fr.obeo.baseliner.ui.builder;

import java.io.FileNotFoundException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import fr.obeo.baseliner.ApiChangeLog;
import fr.obeo.baseliner.MEMApiChangeLog;
import fr.obeo.baseliner.ui.WorkspaceBuilder;

public class BaselinerBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "fr.obeo.baseliner.ui.BaselinerBuilder";

	private HashFunction hashFunction = Hashing.goodFastHash(14);

	public ApiChangeLog changeLog = new MEMApiChangeLog();

	private Baseliner baseliner = new Baseliner();

	private HashCode lastManifestHash;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (WorkspaceBuilder.getInstance().isEnabled()) {
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					BuildTriggerPolicyFromDelta processor = new BuildTriggerPolicyFromDelta(hashFunction,
							lastManifestHash);
					delta.accept(processor);
					if (processor.shouldRebuild()) {
						fullBuild(monitor);
					}
				}
			}
		}
		return null;
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();
			Optional<String> updatedContent = baseliner.doBaseline(monitor, project);
			baseliner.updateAPIReport(monitor, project);
			if (updatedContent.isPresent()) {
				lastManifestHash = hashFunction.hashString(updatedContent.get(), Charsets.UTF_8);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			// TODO log
		} catch (FileNotFoundException e) {
			// TODO log
			e.printStackTrace();
		}
	}

}
