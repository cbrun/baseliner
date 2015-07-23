package fr.obeo.baseliner.bnd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aQute.bnd.differ.Baseline;
import aQute.bnd.differ.Baseline.Info;
import aQute.bnd.differ.DiffPluginImpl;
import aQute.bnd.osgi.Instructions;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import fr.obeo.baseliner.ApiComparator;
import fr.obeo.baseliner.Delta;

public class BndApiComparator implements ApiComparator {

	private Jar oldJar;
	private Jar newJar;

	private Reporter reporter = new Reporter() {

		@Override
		public boolean isOk() {
			return true;
		}

		@Override
		public List<String> getWarnings() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public Location getLocation(String msg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getErrors() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public SetLocation warning(String format, Object... args) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void trace(String format, Object... args) {
			// TODO Auto-generated method stub

		}

		@Override
		public void progress(float progress, String format, Object... args) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isPedantic() {
			return false;
		}

		@Override
		public SetLocation exception(Throwable t, String format, Object... args) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SetLocation error(String format, Object... args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Override
	public void loadNewClassesFromFolder(File projectfolder, File folder) throws RuntimeException {
		try {
			newJar = new WorkspaceJar(projectfolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void loadOldClassesFromFolder(File jarFile) throws RuntimeException {
		try {
			oldJar = new Jar(jarFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public String getOldVersion() {
		try {
			return oldJar.getVersion();
		} catch (Exception e) {
		
		}
		return null;
	}
	

	@Override
	public Map<String, Delta> diffByPackage() {
		Map<String, Delta> result = new HashMap<String, Delta>();
		try {
			if (oldJar != null && newJar != null) {
				Baseline baseline = new Baseline(reporter, CachedDiffer.GLOBAL_DIFFER);
				Set<Info> infos = baseline.baseline(newJar, oldJar, new Instructions("*"));
				for (Info info : infos) {
					if (info.suggestedVersion != null)
						result.put(info.packageName, new BndDelta(info));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
