/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.basic.core.workspace.PathInfo;
import com.top_logic.basic.core.workspace.Workspace;

/**
 * The class {@link CompileJSP} provides {@link Test}s for syntactical
 * correctness of jsp files, i.e. the tests checks that the jsp's compile.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class CompileJSP {

	/**
	 * Whether the translated Java files should be compiled with JavaC instead of Jasper itself.
	 * <p>
	 * The most important difference: Jasper uses the JDTCompiler, which is buggy. If there is a
	 * class which has the same name as a package, then this package is not identified as a package.
	 * JavaC does not have this problem. To be as compatible as possible with all compilers, Jasper
	 * has to be used. It will fail to compile those JSPs and the developer can change them to be
	 * compilable even with a buggy compiler.
	 * </p>
	 */
	public static final boolean USE_JAVAC = false;

	static final String TMP_DIR = "tmp/jsp";

	/** {@link FileFilter} accepting files that ends with "jsp" */
	static FileFilter JSP_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith("jsp");
		}
	};

	static FileFilter DIR_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};

	/** {@link FileFilter} accepting directories and files accepted by {@value #JSP_FILTER} */
	static FileFilter JSP_DIR_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || JSP_FILTER.accept(pathname);
		}
	};

	private static final FilenameFilter JAR_FILE_FILTER = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	/** Version of the java code contained on the JSP's. */
	public static final String SOURCE_CODE_VERSION = "1.8";

	/** Version of VM to compile JSP's for. */
	public static final String TARGET_VM_VERSION = "1.8";

	/**
	 * Constant indicating that jsp's in subfolders must also be tested.
	 * 
	 * @see TestCompileJSPs#compile(File, String, boolean)
	 */
	public static final boolean RECURSIVE = true;

	static List<String> getTranslateCommand(CompileJspConfig config, String targetPckg, Collection<File> jspFiles)
			throws IOException {
		List<String> command = new ArrayList<>();
		
		addJavaCommand(command);
		addTempDir(command);
		addClassPath(command);
		/*
		 * For debugging reason
		 */
//		command.add("-ea");
//		command.add("-Xdebug");
//		command.add("-Xrunjdwp:transport=dt_socket,address=9095,server=y,suspend=y");

		command.add("org.apache.jasper.JspC");
		if (!USE_JAVAC) {
			command.add("-compile");
		}
		command.add("-webapp");
		command.add(config.getWebapp().getCanonicalPath());

		if (config.isVerbose()) {
			command.add("-v");
		}

		command.add("-source");
		command.add(SOURCE_CODE_VERSION);
		command.add("-target");
		command.add(TARGET_VM_VERSION);

		command.add("-p");
		command.add(targetPckg);

		if (config.getWebXml() != null) {
			command.add("-webxml");
			command.add(config.getWebXml());
		} else if (config.getWebXmlFragment() != null) {
			command.add("-webinc");
			command.add(config.getWebXmlFragment());
		}
		if (config.getOutputDir() != null) {
			command.add("-d");
			command.add(config.getOutputDir());
		}

		for (File file : jspFiles) {
			command.add(file.getCanonicalPath());
		}
		return command;
	}

	static void addTempDir(List<String> command) {
		command.add("-Djava.io.tmpdir=" + TMP_DIR);
	}

	static void addJavaCommand(List<String> command) throws IOException {
		command.add(javaExecutable());
	}

	private static String javaExecutable() throws IOException {
		String javaHomeProp = System.getProperty("java.home");
		File javaHome = new File(javaHomeProp);
		if (!javaHome.exists()) {
			throw new IOException("Java-Home '" + javaHomeProp  + "' does not exist.");
		}
		String javaExecutable = javaHome.getCanonicalPath() + File.separatorChar + "bin" + File.separatorChar + "java";
		return javaExecutable;
	}

	static void addClassPath(List<String> command) throws IOException {
		command.add("-classpath");

		StringBuilder path = new StringBuilder();
		
		PathInfo paths = Workspace.getAppPaths();
		path.append(paths.getClassPath().stream().map(f -> f.getAbsolutePath())
			.collect(Collectors.joining(File.pathSeparator)));
		command.add(path.toString());
	}

	static <T extends Collection<File>> T findFiles(T files, File root, FileFilter filter) {
		if (root.isDirectory()) {
			final File[] subDirs = root.listFiles(filter);
			for (File f : subDirs) {
				findFiles(files, f, filter);
			}
		} else {
			files.add(root);
		}
		return files;
	}

}
