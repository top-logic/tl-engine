/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;

/**
 * Algorithm for building the resource path of an application by inspecting its classpath.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PathInfo {

	private static final Logger LOG = Logger.getLogger(PathInfo.class.getName());

	private final ModelReader _analyzer = new DefaultModelReader();

	private final String _deployDir;

	private final String[] _deployAspects;

	private List<URL> _resourcePath = new ArrayList<>();

	private List<Path> _resourceDirs = new ArrayList<>();

	private File _toplevelProjectPath;

	private List<File> _classFolders = new ArrayList<>();

	private List<URL> _classJars = new ArrayList<>();

	private List<File> _classPath = new ArrayList<>();

	private Set<File> _classPathEnries = new HashSet<>();

	private Map<String, List<Runnable>> _builderById = new HashMap<>();

	private final DependencyResolver _resolver = new DependencyResolver();

	/**
	 * Creates a {@link PathInfo}.
	 */
	PathInfo(String deployDir, String[] deployAspects) throws IOException {
		_deployDir = deployDir;
		_deployAspects = deployAspects;
		_toplevelProjectPath = new File(".").getCanonicalFile();
	}

	/**
	 * The collected classpath.
	 */
	public List<File> getClassPath() {
		return _classPath;
	}

	/**
	 * The resource path.
	 */
	public List<URL> getResourcePath() {
		return _resourcePath;
	}

	/**
	 * The resource path reduced to those elements that are directories in the file system (not war
	 * fragments).
	 */
	public List<Path> getResourceDirs() {
		return _resourceDirs;
	}

	/**
	 * All <code>classes</code> folders of the {@link #getClassPath()}.
	 */
	public List<File> getClassFolders() {
		return _classFolders;
	}

	/**
	 * All <code>jar</code> resources on the {@link #getClassPath()}.
	 */
	public List<URL> getClassJars() {
		return _classJars;
	}

	boolean addClasspathEntry(File entryFile) {
		if (!_classPathEnries.add(entryFile)) {
			return false;
		}
		_classPath.add(entryFile);
		return true;
	}

	void addClassesDir(File classesPath) {
		_classFolders.add(classesPath);
	}

	void addProject(File projectPath, boolean isTest) throws IOException {
		File pomFile = new File(projectPath, "pom.xml");
		if (pomFile.exists()) {
			Model projectModel = _analyzer.read(pomFile, Collections.emptyMap());
			addPart(projectModel, isTest, () -> doAddProject(projectPath, isTest));
		} else {
			doAddProject(projectPath, isTest);
		}
	}

	private void doAddProject(File projectPath, boolean isTest) {
		String webappDir;
		if (isTest) {
			webappDir = ModuleLayoutConstants.TEST_WEBAPP_DIR;
		} else {
			webappDir = ModuleLayoutConstants.WEBAPP_DIR;

			addDeployAspects(projectPath);
		}

		File webappPath = new File(projectPath, webappDir);
		if (webappPath.exists()) {
			addWebappPath(webappPath);
		}
	}

	void addJar(File jarFile) throws IOException {
		FileSystem fileSystem = FileSystems.newFileSystem(jarFile.toPath(), null);

		Path base = fileSystem.getPath("META-INF", "maven");
		if (Files.exists(base)) {
			Optional<Path> pomPath =
				Files.walk(base).filter(p -> p.getFileName().toString().equals("pom.xml")).findFirst();
			if (pomPath.isPresent()) {
				Model projectModel = _analyzer.read(Files.newInputStream(pomPath.get()), Collections.emptyMap());
				addPart(projectModel, jarFile.getName().endsWith("-tests.jar"), () -> doAddJar(jarFile));
				return;
			}
		}
		doAddJar(jarFile);
	}

	private void doAddJar(File jarFile) {
		_classJars.add(url(jarFile));
		String jarName = jarFile.getName();
		File repositoryFolder = jarFile.getParentFile();
		String fragmentName =
			jarName.substring(0, jarName.length() - ".jar".length()) + "-web-fragment.war";
		File fragmentFile = new File(repositoryFolder, fragmentName);
		if (fragmentFile.exists()) {
			addFragmentWar(fragmentFile);
		} else {
			LOG.fine("No web fragment found for classpath entry: " + jarFile);
		}
	}

	private void addPart(Model projectModel, boolean isTest, Runnable part) {
		String id = _resolver.enter(projectModel, isTest);

		// Note: For a single project model there may be multiple JAR artifacts in the class path:
		// The main JAR and the test JAR. Therefore, multiple builders must be kept.
		_builderById.computeIfAbsent(id, ignore -> new ArrayList<>()).add(part);
	}

	private void addWebappPath(File webappPath) {
		addResourcePath(webappPath);
		addResourceDir(webappPath.toPath());
	}

	private void addFragmentWar(File fragmentWar) {
		addResourcePath(fragmentWar);
		try {
			for (Path root : FileSystems.newFileSystem(fragmentWar.toPath(), null).getRootDirectories()) {
				addResourceDir(root);
			}
		} catch (IOException ex) {
			LOG.log(Level.WARNING, "Cannot access '" + fragmentWar + "'.", ex);
		}
	}

	private void addResourceDir(Path root) {
		_resourceDirs.add(root);
	}

	private void addResourcePath(File deployFolder) {
		URL url = url(deployFolder);
		_resourcePath.add(url);
	}

	private URL url(File deployFolder) {
		try {
			return deployFolder.toURI().toURL();
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addDeployAspects(File projectPath) {
		if (_deployAspects.length == 0) {
			return;
		}
		File deploydir = new File(projectPath, _deployDir);
		if (!deploydir.isDirectory()) {
			return;
		}

		boolean addLocalAspect = projectPath.equals(_toplevelProjectPath);

		for (int i = _deployAspects.length - 1; i >= 0; i--) {
			String deployAspect = _deployAspects[i];
			if (ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME.equals(deployAspect) && !addLocalAspect) {
				// "deploy local" folder must be skipped for all projects other than the top-level
				// one.
				continue;
			}
			File deployAspectRoot = new File(deploydir, deployAspect);
			if (!deployAspectRoot.isDirectory()) {
				continue;
			}
			File deployWebapp = Workspace.webappDeploy(deployAspectRoot);
			if (deployWebapp != null) {
				addDeployFolder(deployWebapp);
			}
		}
	}

	private void addDeployFolder(File deployFolder) {
		addResourcePath(deployFolder);
		addResourceDir(deployFolder.toPath());
	}

	PathInfo complete() {
		for (Runnable builder : buildersTopologicallySorted()) {
			builder.run();
		}

		return this;
	}

	private List<Runnable> buildersTopologicallySorted() {
		List<String> buildOrder = _resolver.createBuildOrder();
		Collections.reverse(buildOrder);

		return buildOrder.stream()
			.map(_builderById::get)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

}
