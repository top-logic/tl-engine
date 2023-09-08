/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.core.workspace.eclipse.SafeChunkyInputStream;

/**
 * Abstraction of an Eclipse workspace.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Workspace {

	/**
	 * System property to communicate the current workspace location to tools.
	 */
	// TODO #26368: Remove.
	public static final String WORKSPACE_PROPERTY = "eclipse.workspace";

	/* package */static final String URI_PREFIX = "URI//"; //$NON-NLS-1$

	private static final Logger LOG = Logger.getLogger(Workspace.class.getName());

	private final File _root;

	/**
	 * Creates a {@link Workspace}.
	 * 
	 * @param root
	 *        The workspace folder.
	 */
	protected Workspace(File root) {
		_root = root;
	}

	/**
	 * Builds a web application resource path for the current classpath of the running JVM.
	 */
	public static PathInfo getAppPaths() throws IOException {
		return getAppPaths(ModuleLayoutConstants.DEPLOY_FOLDER_NAME, ModuleLayout.DEFAULT_DEPLOY_ASPECTS);
	}

	/**
	 * Builds a web application resource path for the current classpath of the running JVM.
	 */
	public static PathInfo getAppPaths(String deployDir, String[] deployAspects) throws IOException {
		PathInfo result = new PathInfo(deployDir, deployAspects);

		ClassLoader loader = Workspace.class.getClassLoader();
		if (loader instanceof URLClassLoader) {
			URL[] urls = ((URLClassLoader) loader).getURLs();
			for (URL url : urls) {
				try {
					Path path = Paths.get(url.toURI());
					addClassPathEntry(result, path);
				} catch (URISyntaxException ex) {
					LOG.warning("Unsupported classpath entry, cannot resolve to file: " + url);
				}
			}
		}

		// Note: When running from the Maven command line with forked test execution, the
		// classloader does not provide any usable URLs.
		String classPath = System.getProperty("java.class.path");
		String[] pathEntries = classPath.split(Pattern.quote(File.pathSeparator));

		for (String pathEntry : pathEntries) {
			addClassPathEntry(result, Paths.get(pathEntry));
		}

		result = result.complete();
		LOG.info("Using resource paths: " + result.getResourcePath());
		return result;
	}

	private static void addClassPathEntry(PathInfo result, Path pathEntry) throws IOException {
		if (!Files.exists(pathEntry)) {
			LOG.warning("Unsupported classpath entry, cannot resolve to file: " + pathEntry);
			return;
		}

		File pathEntryFile = pathEntry.normalize().toFile();
		/* Ensure that links are followed. */
		pathEntryFile = pathEntryFile.getCanonicalFile();
		boolean ok = result.addClasspathEntry(pathEntryFile);
		if (!ok) {
			// Was already found.
			return;
		}

		if (pathEntryFile.isDirectory()) {
			boolean isTest;
			if (pathEntryFile.getName().equals(ModuleLayoutConstants.CLASSES_DIR)) {
				isTest = false;
			} else if (pathEntryFile.getName().equals(ModuleLayoutConstants.TEST_CLASSES_DIR)) {
				isTest = true;
			} else {
				return;
			}
			File buildPath = pathEntryFile.getParentFile();
			if (!buildPath.getName().equals(ModuleLayoutConstants.TARGET_DIR)) {
				return;
			}

			result.addClassesDir(pathEntryFile);

			File projectPath = buildPath.getParentFile();
			result.addProject(projectPath, isTest);
		} else {
			String jarName = pathEntryFile.getName();
			if (jarName.endsWith(".jar")) {
				result.addJar(pathEntryFile);
			} else {
				LOG.warning("Unsupported classpath entry with unknown extension: " + pathEntry);
				return;
			}
		}
	}

	/**
	 * The directory of the Eclipse project with the given name.
	 */
	public File resolveProjectDir(String projectName) throws IOException {
		File result = resolveProjectDirOrNull(projectName);
		if (result == null) {
			throw new IOException("Cannot resolve project dir for '" + projectName + "': '" + _root.getAbsolutePath()
				+ "' is neither an Eclipse workspace nor does it have a subfolder '" + projectName + "'.");
		}
		return result;
	}

	/**
	 * The directory of the Eclipse project with the given name, or <code>null</code> if no such
	 * directory can be found.
	 */
	public File resolveProjectDirOrNull(String projectName) throws IOException {
		File firstGuess = new File(_root, projectName);
		if (firstGuess.isDirectory()) {
			return firstGuess;
		}

		// Assume an Eclipse workspace structure.
		File location = new File(_root,
			".metadata/.plugins/org.eclipse.core.resources/.projects/" + projectName + "/.location");
		if (!location.exists()) {
			return null;
		}
		try (DataInputStream in = new DataInputStream(new SafeChunkyInputStream(location))) {
			String locationDescr = in.readUTF();
			if (locationDescr.length() > 0) {
				// location format < 3.2 was a local file system OS path
				// location format >= 3.2 is: URI_PREFIX + uri.toString()
				File locationDir;
				if (locationDescr.startsWith(URI_PREFIX)) {
					URI uri = URI.create(locationDescr.substring(URI_PREFIX.length()));
					locationDir = Paths.get(uri).toFile();
				} else {
					LOG.info("Non-URI in Eclipse location descriptor: " + locationDescr);
					locationDir = new File(locationDescr);
				}
				if (locationDir.exists()) {
					return locationDir;
				}
				LOG.info("Referenced module '" + projectName
					+ "' does not exist in location described by Eclipse descriptor: " + locationDir);
			}
		}

		return null;
	}

	/**
	 * The web application folders defined by a project.
	 */
	public static File webappDeploy(File deployDir) {
		File webapp = new File(deployDir, ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME);
		if (!webapp.isDirectory()) {
			return null;
		}
		return webapp;
	}

	static boolean isRegularDir(File pathname) {
		return pathname.isDirectory() && !pathname.isHidden() && !pathname.getName().startsWith(".");
	}

	static boolean fileExists(File file) {
		if (!file.exists()) {
			return false;
		}
		if (!file.isFile()) {
			return false;
		}
		return true;
	}

	/**
	 * Create the paths of the currently running modular web application.
	 */
	public static List<Path> applicationModules(String... deployAspects) {
		final String resourcepath = Environment.getSystemPropertyOrEnvironmentVariable("tl_resource_path", null);
		if (resourcepath != null) {
			// use externally provided resource path
			return Stream.of(resourcepath.split(File.pathSeparator)).map(Paths::get).collect(Collectors.toList());
		}
		String[] deployAspectsWithDefaults = deployAspects.length == 0 ? ModuleLayout.DEFAULT_DEPLOY_ASPECTS : deployAspects;
		try {
			return getAppPaths(ModuleLayoutConstants.DEPLOY_FOLDER_NAME, deployAspectsWithDefaults).getResourceDirs();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * The project folder for a web application.
	 */
	public static File projectDir(File webapp) {
		File project;
		try {
			project = new File(webapp, ModuleLayoutConstants.PATH_TO_MODULE_ROOT).getCanonicalFile();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return project;
	}

	/**
	 * The workspace root for a web application root directory.
	 * 
	 * @param webapp
	 *        The web application's root folder.
	 */
	public static File webappWorkspaceRoot(File webapp) {
		return projectWorkspaceRoot(Workspace.projectDir(webapp));
	}

	/**
	 * The workspace root for a project directory.
	 * 
	 * @param project
	 *        The Eclipse module directory of a web application module.
	 */
	public static File projectWorkspaceRoot(File project) {
		String workspace = System.getProperty(WORKSPACE_PROPERTY);
		if (workspace == null) {
			return project.getParentFile();
		}
		return new File(workspace);
	}

	/**
	 * Creates a {@link Workspace} for a given web application path.
	 */
	public static Workspace createWorkspaceForWebapp(File webapp) {
		return createWorkspaceForProject(Workspace.projectDir(webapp));
	}

	/**
	 * Creates a {@link Workspace} for a given root folder.
	 */
	public static Workspace createWorkspace(File root) {
		return new Workspace(root);
	}

	/**
	 * Creates an Eclipse workspace assuming the current working directory is the root folder of
	 * some Eclipse project.
	 */
	public static Workspace createWorkspace() {
		return createWorkspaceForProject(pwd());
	}

	private static Workspace createWorkspaceForProject(File projectDir) {
		return createWorkspace(Workspace.projectWorkspaceRoot(projectDir));
	}

	/**
	 * Resolves the current working directory to a canonical path.
	 *
	 * <p>
	 * The absolute canonical location of the current working directory must be resolved in order to
	 * be able to correctly work with the File-API in if <code>user.dir</code> has been modified
	 * from within the currently running JVM. This happens when the application is invoked from
	 * within a Maven task.
	 * </p>
	 */
	public static File pwd() {
		try {
			return new File(".").getCanonicalFile();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}

