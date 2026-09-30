/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Stream;

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

	/**
	 * System property holding the primary local Maven repository.
	 */
	private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

	/**
	 * System property holding a comma-separated list of local Maven repositories searched after
	 * {@link #MAVEN_REPO_LOCAL}.
	 */
	private static final String MAVEN_REPO_LOCAL_TAIL = "maven.repo.local.tail";

	/**
	 * Jar entry marking a TopLogic module that comes with a web fragment.
	 */
	private static final String MODULE_WITH_RESOURCES_MARKER = "META-INF/tl-module-with-resources";

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
			addPart(projectModel, isTest, () -> doAddProject(projectPath, projectModel, isTest));
		} else {
			doAddProject(projectPath, null, isTest);
		}
	}

	private void doAddProject(File projectPath, Model projectModel, boolean isTest) {
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

		if (!isTest && projectModel != null) {
			addOverlayWebapps(projectPath, projectModel);
		}
	}

	/**
	 * Scans the project's POM for WAR overlay dependencies (web-fragment WARs) and includes
	 * the corresponding sibling project's webapp paths. This is needed because WAR-type
	 * dependencies are not on the classpath, so their webapp resources would otherwise not be
	 * found in IDE mode.
	 */
	private void addOverlayWebapps(File projectPath, Model projectModel) {
		File workspaceRoot = projectPath.getParentFile();
		for (org.apache.maven.model.Dependency dep : projectModel.getDependencies()) {
			if (!"war".equals(dep.getType())) {
				continue;
			}
			if (!"web-fragment".equals(dep.getClassifier())) {
				continue;
			}

			// Look for the overlay project as a sibling directory in the workspace.
			File overlayProject = findModuleDir(workspaceRoot, dep.getArtifactId());
			if (overlayProject == null) {
				continue;
			}

			File overlayWebapp = new File(overlayProject, ModuleLayoutConstants.WEBAPP_DIR);
			if (overlayWebapp.exists()) {
				LOG.fine("Including overlay webapp from: " + overlayWebapp);
				addWebappPath(overlayWebapp);
			}
		}
	}

	/**
	 * Finds the workspace directory for the given Maven artifactId by scanning the
	 * workspace root for a directory whose pom.xml declares that artifactId. Falls back
	 * to a naming convention ("tl-foo-bar" -> "com.top_logic.foo.bar").
	 */
	private File findModuleDir(File workspaceRoot, String artifactId) {
		// Try naming convention first (fast path).
		String conventionName;
		if (artifactId.startsWith("tl-")) {
			conventionName = "com.top_logic." + artifactId.substring("tl-".length()).replace('-', '.');
		} else {
			conventionName = artifactId;
		}
		File conventionDir = new File(workspaceRoot, conventionName);
		if (conventionDir.isDirectory()) {
			return conventionDir;
		}

		// Fallback: scan workspace directories for matching artifactId.
		File[] candidates = workspaceRoot.listFiles(File::isDirectory);
		if (candidates != null) {
			for (File dir : candidates) {
				File pom = new File(dir, "pom.xml");
				if (pom.exists()) {
					try {
						Model model = _analyzer.read(pom, Collections.emptyMap());
						if (artifactId.equals(model.getArtifactId())) {
							return dir;
						}
					} catch (IOException ex) {
						// Skip unreadable POMs.
					}
				}
			}
		}
		return null;
	}

	void addJar(File jarFile) throws IOException {
		try (FileSystem fileSystem = FileSystems.newFileSystem(jarFile.toPath())) {
			boolean moduleWithResources = Files.exists(fileSystem.getPath(MODULE_WITH_RESOURCES_MARKER));

			Path base = fileSystem.getPath("META-INF", "maven");
			if (Files.exists(base)) {
				Optional<Path> pomPath;
				try (Stream<Path> entries = Files.walk(base)) {
					pomPath = entries.filter(p -> p.getFileName().toString().equals("pom.xml")).findFirst();
				}
				if (pomPath.isPresent()) {
					Model projectModel;
					try (InputStream in = Files.newInputStream(pomPath.get())) {
						projectModel = _analyzer.read(in, Collections.emptyMap());
					}
					String coordinateDir = coordinateDir(jarFile, base.relativize(pomPath.get()));
					addPart(projectModel, jarFile.getName().endsWith("-tests.jar"),
						() -> doAddJar(jarFile, coordinateDir, moduleWithResources));
					return;
				}
			}
			doAddJar(jarFile, null, moduleWithResources);
		}
	}

	/**
	 * The directory of the given jar relative to the root of a Maven repository.
	 *
	 * @param jarFile
	 *        The jar file whose parent directory is the version directory of its artifact.
	 * @param pomEntry
	 *        The path of the jar's POM entry relative to <code>META-INF/maven</code>, i.e.
	 *        <code>&lt;groupId&gt;/&lt;artifactId&gt;/pom.xml</code>.
	 * @return The path <code>&lt;groupId as path&gt;/&lt;artifactId&gt;/&lt;version&gt;</code>, or
	 *         <code>null</code> if the POM entry does not have the expected structure.
	 */
	private static String coordinateDir(File jarFile, Path pomEntry) {
		if (pomEntry.getNameCount() != 3) {
			return null;
		}
		String groupId = pomEntry.getName(0).toString();
		String artifactId = pomEntry.getName(1).toString();
		String version = jarFile.getParentFile().getName();
		return groupId.replace('.', '/') + '/' + artifactId + '/' + version;
	}

	private void doAddJar(File jarFile, String coordinateDir, boolean moduleWithResources) {
		_classJars.add(url(jarFile));
		String jarName = jarFile.getName();
		String fragmentName =
			jarName.substring(0, jarName.length() - ".jar".length()) + "-web-fragment.war";
		List<File> candidates = fragmentCandidates(jarFile, coordinateDir, fragmentName);
		for (File fragmentFile : candidates) {
			if (fragmentFile.exists()) {
				addFragmentWar(fragmentFile);
				return;
			}
		}
		if (moduleWithResources) {
			LOG.warning("No web fragment found for module '" + jarFile + "', searched: " + candidates);
		} else {
			LOG.fine("No web fragment found for classpath entry: " + jarFile);
		}
	}

	/**
	 * The locations where the web fragment of the given jar may reside, in search order.
	 *
	 * <p>
	 * The first candidate is the directory of the jar itself. If the artifact coordinates of the
	 * jar are known, the coordinate directory in each local Maven repository of the chain
	 * {@link #MAVEN_REPO_LOCAL}, {@link #MAVEN_REPO_LOCAL_TAIL} follows, since Maven may resolve the
	 * jar and its fragment from different repositories of that chain.
	 * </p>
	 */
	private static List<File> fragmentCandidates(File jarFile, String coordinateDir, String fragmentName) {
		List<File> result = new ArrayList<>();
		result.add(new File(jarFile.getParentFile(), fragmentName));
		if (coordinateDir != null) {
			for (String repository : localRepositories()) {
				File candidate = new File(new File(repository, coordinateDir), fragmentName);
				if (!result.contains(candidate)) {
					result.add(candidate);
				}
			}
		}
		return result;
	}

	/**
	 * The local Maven repositories in the order Maven resolves artifacts from them.
	 */
	private static List<String> localRepositories() {
		List<String> result = new ArrayList<>();
		String local = System.getProperty(MAVEN_REPO_LOCAL);
		if (local != null && !local.isBlank()) {
			result.add(local.trim());
		}
		String tail = System.getProperty(MAVEN_REPO_LOCAL_TAIL);
		if (tail != null) {
			for (String entry : tail.split(",")) {
				if (!entry.isBlank()) {
					result.add(entry.trim());
				}
			}
		}
		return result;
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
			for (Path root : FileSystems.newFileSystem(fragmentWar.toPath()).getRootDirectories()) {
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
