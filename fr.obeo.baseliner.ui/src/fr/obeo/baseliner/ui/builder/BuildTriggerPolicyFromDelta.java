package fr.obeo.baseliner.ui.builder;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.Files;

public class BuildTriggerPolicyFromDelta implements IResourceDeltaVisitor {

	private HashCode lastManifestCode;

	private boolean shouldRebuild = false;

	private HashFunction hashFunction;

	public BuildTriggerPolicyFromDelta(HashFunction hashfunc, HashCode lastManifestCode) {
		this.lastManifestCode = lastManifestCode;
		this.hashFunction = hashfunc;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		// only interested in changed resources (not added or removed)
		if (delta.getKind() == IResourceDelta.MARKERS || delta.getKind() == IResourceDelta.NO_CHANGE)
			return true;
		// only interested in content changes
		if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
			return true;
		IResource resource = delta.getResource();
		// only interested in files with the "txt" extension
		if (resource.getType() == IResource.FILE) {
			if ("fr.obeo.baseliner.prefs".equals(resource.getName())) {
				shouldRebuild = true;
			}
			if ("MANIFEST.MF".equals(resource.getName())) {
				/*
				 * we have an event on a Manifest file.
				 */
				if (lastManifestCode != null) {

					try {
						HashCode newCode = Files.hash(((IFile) resource).getLocation().toFile(), hashFunction);
						if (!lastManifestCode.equals(newCode))
							shouldRebuild = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				return true;
			} else {
				if ("class".equalsIgnoreCase(resource.getFileExtension())) {
					shouldRebuild = true;
				}
			}
		}
		return true;
	}

	public boolean shouldRebuild() {
		return shouldRebuild;
	}

}
