package org.osjava.jardiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.osjava.jardiff.ClassInfo;
import org.osjava.jardiff.JarDiff;

public class ExtendedJarDiff extends JarDiff {

	public void loadNewClassFile(File classFile) throws IOException {
		FileInputStream in = new FileInputStream(classFile);
		try {
			ClassReader reader = new ClassReader(in);
			ClassInfo ci = loadClassInfo(reader);
			newClassInfo.put(ci.getName(), ci);
		} finally {
			if (in != null) {
				in.close();
			}
		}

	}
}
