package fr.obeo.baseliner.bnd;

import java.util.Map;

import aQute.bnd.differ.DiffPluginImpl;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.service.diff.Differ;
import aQute.bnd.service.diff.Tree;
import aQute.bnd.service.diff.Tree.Data;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.collect.*;

public class CachedDiffer implements Differ {

	public static final Differ GLOBAL_DIFFER = new CachedDiffer(
			new DiffPluginImpl());

	private Differ delegate;

	private HashFunction hashFunction = Hashing.goodFastHash(128);

	private Map<HashCode, Tree> cachedTrees = Maps.newHashMap();

	public CachedDiffer(Differ delegate) {
		this.delegate = delegate;
	}

	@Override
	public Tree tree(Analyzer source) throws Exception {
		Tree result = delegate.tree(source);
		return result;
	}

	@Override
	public Tree tree(Jar jar) throws Exception {
		Tree result = null;
		if (jar.getSource() != null && jar.getSource().isFile()) {
			HashCode newCode = Files.hash(jar.getSource(), hashFunction);
			result = cachedTrees.get(newCode);
			if (result == null) {
				result = delegate.tree(jar);
			}
			cachedTrees.put(newCode, result);
		}
		if (result == null) {
			result = delegate.tree(jar);
		}
		return result;
	}

	@Override
	public Tree deserialize(Data data) throws Exception {
		Tree result = delegate.deserialize(data);
		return result;
	}

}
