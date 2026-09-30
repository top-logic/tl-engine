package com.top_logic.build.maven.war;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.util.StringUtils;

import com.top_logic.basic.core.workspace.DependencyResolver;
import com.top_logic.build.maven.resolve.EngineResolver;
import com.top_logic.build.maven.war.tasks.AppOverlay;
import com.top_logic.build.maven.war.tasks.DeployOverlay;
import com.top_logic.build.maven.war.tasks.FragmentOverlay;
import com.top_logic.build.maven.war.tasks.WarTask;

/**
 * Goal that builds a runnable TopLogic application WAR.
 */
@Mojo(name = TLAppWar.GOAL, defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TLAppWar extends AbstractMojo {

	/**
	 * The name of the goal implemented by {@link TLAppWar}.
	 */
	public static final String GOAL = "app";

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project.build.finalName}", required = true, readonly = true)
	private String baseName;

	/**
	 * The name of the generated WAR artifact.
	 * 
	 * <p>
	 * The default value is based on the {@link #baseName} and {@link #appClassifier}.
	 * </p>
	 */
	@Parameter(required = false, readonly = true)
    private String warName;

	// Only available since maven-archiver 3.5.2, but this has a problem with excessive memory
	// consumption causing builds to fail with OOM.
	//
	// **
	// * Timestamp for reproducible output archive entries, either formatted as ISO 8601
	// * <code>yyyy-MM-dd'T'HH:mm:ssXXX</code> or as an int representing seconds since the epoch (like
	// * <a href="https://reproducible-builds.org/docs/source-date-epoch/">SOURCE_DATE_EPOCH</a>).
	// */
	// @Parameter(defaultValue = "${project.build.outputTimestamp}")
	// private String outputTimestamp;

	/**
	 * The archive configuration to use. See
	 * <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver
	 * Reference</a>.
	 */
	@Parameter
	private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

	@Parameter(defaultValue = "false")
	private boolean includeEmptyDirectories;

	/**
	 * Indicates if zip archives (jar,zip etc) being added to the war should be compressed again.
	 * Compressing again can result in smaller archive size, but gives noticeably longer execution
	 * time.
	 *
	 * @since 2.3
	 */
	@Parameter(defaultValue = "true")
	private boolean recompressZippedFiles;

	/**
	 * The base directory, where {@link #webappDirectory} is created.
	 */
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File buildDirectory;

	/**
	 * The directory where the expanded web application is built before archiving.
	 * 
	 * <p>
	 * The default value is based on {@link #baseName} and {@link #appClassifier}.
	 * </p>
	 */
	@Parameter(required = false)
	private File webappDirectory;
	
    /**
     * The comma separated list of tokens to exclude from the WAR before packaging. This option may be used to implement
     * the skinny WAR use case. Note that you can use the Java Regular Expressions engine to include and exclude
     * specific pattern using the expression %regex[]. Hint: read the about (?!Pattern).
     *
     * @since 2.1-alpha-2
     */
    @Parameter
    private String packagingExcludes;

    /**
     * The comma separated list of tokens to include in the WAR before packaging. By default everything is included.
     * This option may be used to implement the skinny WAR use case. Note that you can use the Java Regular Expressions
     * engine to include and exclude specific pattern using the expression %regex[].
     *
     * @since 2.1-beta-1
     */
	@Parameter(defaultValue = "**")
	private String packagingIncludes;

	/**
	 * Comma-separated list of regular expression patterns that match configuration references found
	 * the application's <code>metaConf.txt</code> to exclude during deployment.
	 */
	@Parameter(defaultValue = "devel-.*", property = "tl.app.metaConfExcludes")
	private String metaConfExcludes;

	/**
	 * Comma-separated list of configuration file references to add to the <code>metaConf.txt</code>
	 * during deployment.
	 * 
	 * <p>
	 * To to prepare the deployed application to use the H2 database, you can e.g. set the value to
	 * <code>db-h2.xml</code> to include the corresponding configuration reference to its
	 * configuration.
	 * </p>
	 */
	@Parameter(defaultValue = "", property = "tl.app.metaConfAddons")
	private String metaConfAddons;

    /**
     * The WAR archiver.
     */
    @Component( role = Archiver.class, hint = "war" )
    private WarArchiver warArchiver;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    @Component
    private MavenProjectHelper projectHelper;
    
    @Component
    private RepositorySystem repositorySystem;
    
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    private List<ArtifactRepository> pomRemoteRepositories;
    
    @Component
    private ArtifactResolver artifactResolver;
    
    @Component
    private ArtifactHandlerManager artifactHandlerManager;

	/**
	 * The classifier to use for the app WAR artifact.
	 * 
	 * @see #attach
	 */
	@Parameter(defaultValue = "app", readonly = true, required = true)
	private String appClassifier;

	/**
	 * The directory where local deploy folders are located.
	 */
	@Parameter(defaultValue = "${basedir}/deploy", required = true)
	private File deployDirectory;

	/**
	 * The directory within the deploy aspect directory where web resources are located.
	 */
	@Parameter(defaultValue = "webapp", required = false)
	private String deployWebapp;

    /**
	 * The overlays to apply.
	 * 
	 * <p>
	 * Each &lt;overlay&gt; element may contain:
	 * </p>
	 * <ul>
	 * <li>groupId (if this and artifactId are null, then the current project is treated as its own
	 * overlay)</li>
	 * <li>artifactId (see above)</li>
	 * <li>classifier</li>
	 * <li>type</li>
	 * <li>skip (defaults to false)</li>
	 * <li><code>deployAspects</code>, see {@link Overlay#getDeployAspects()}</li>
	 * </ul>
	 */
	@Parameter
	private List<Overlay> overlays = new ArrayList<>();

	/**
	 * Skips the app generation.
	 */
	@Parameter(property = "tl.app.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Controls whether the app WAR artifact is attached to the module.
	 * 
	 * @see #appClassifier
	 */
	@Parameter(property = "tl.app.attach", defaultValue = "false")
	private boolean attach;

	/**
	 * Directory, where deploy aspect folders are located.
	 * 
	 * @see Overlay#getDeployAspects()
	 */
	public File getDeployDirectory() {
		return deployDirectory;
	}

	/**
	 * Sub directory of a deploy aspect directory where web application resources are located.
	 */
	public String getDeployWebapp() {
		return deployWebapp;
	}

	/**
	 * The directory where the expanded web application is built before archiving.
	 */
	public File getWebappDirectory() {
		if (webappDirectory == null) {
			webappDirectory = new File(buildDirectory, baseName + "-" + appClassifier);
		}
		return webappDirectory;
	}

	private String getWarName() {
		if (warName == null) {
			warName = baseName + "-" + appClassifier + ".war";
		}
		return warName;
	}

	/**
	 * The classifier of the build WAR artifact.
	 */
	public String getAppClassifier() {
		return appClassifier;
	}

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Skipping app generation.");
			return;
		}
		if ("pom".equals(project.getPackaging())) {
			getLog().info("Skipping app generation for POM only module.");
			return;
		}

		getLog().info("Building TL-App " + project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
		
		try {
			ensureOutputDirectory();
			cleanWebapp();
			buildWebapp();
			createWar();
		} catch (IOException | ManifestException | DependencyResolutionRequiredException | MojoRuntimeException ex) {
			throw new MojoExecutionException("Cannot create app WAR: " + extractMessage(ex), ex);
		}
	}

	private String extractMessage(Throwable ex) {
		String result = ex.getMessage();
		if (result == null) {
			result = ex.getClass().getName();
		} else if (!(ex instanceof MojoRuntimeException)) {
			result = ex.getClass().getName() + ": " + result;
		}
		Throwable cause = ex.getCause();
		if (cause == null) {
			return result;
		} else {
			String prefix = result.endsWith(".") ? result.substring(0, result.length() - 1) : result;
			return prefix + ": " + extractMessage(cause);
		}
	}

	private void createWar() throws ManifestException, IOException, DependencyResolutionRequiredException {
		getLog().info("Packaging application");

		MavenArchiver archiver = new MavenArchiver();

		archiver.setArchiver(warArchiver);

		// Only available since maven-archiver 3.5.2, but this has a problem with excessive memory
		// consumption causing builds to fail with OOM.
		//
		// archiver.setCreatedBy("TL Maven Plugin", "com.top_logic.build.maven.war",
		// "tl-maven-plugin");

		File warFile = new File(outputDirectory, getWarName());
		archiver.setOutputFile(warFile);

		// Only available since maven-archiver 3.5.2, but this has a problem with excessive memory
		// consumption causing builds to fail with OOM.
		//
		// configure for Reproducible Builds based on outputTimestamp value
		// archiver.configureReproducible(outputTimestamp);

		String[] excludes = parsePatterns(packagingExcludes);
		String[] includes = parsePatterns(packagingIncludes);

		getLog().debug("Excluding " + Arrays.asList(excludes) + " from the generated webapp archive.");
		getLog().debug("Including " + Arrays.asList(includes) + " in the generated webapp archive.");

		warArchiver.addDirectory(getWebappDirectory(), includes, excludes);

		final File webXmlFile = new File(getWebappDirectory(), "WEB-INF/web.xml");
		if (webXmlFile.exists()) {
			warArchiver.setWebxml(webXmlFile);
		}

		warArchiver.setRecompressAddedZips(recompressZippedFiles);
		warArchiver.setIncludeEmptyDirs(includeEmptyDirectories);

		archiver.createArchive(session, project, archive);

		if (attach) {
			projectHelper.attachArtifact(project, "war", appClassifier, warFile);
		}
	}

	private String[] parsePatterns(String patternList) {
		if (StringUtils.isEmpty(patternList)) {
			return new String[0];
		} else {
			return StringUtils.split(patternList, ",");
		}
	}

	private void buildWebapp() throws IOException, MojoExecutionException {
		Map<String, WarTask> tasks = new HashMap<>();
		Map<String, Artifact> fragmentById = new HashMap<>();
		
		DependencyResolver resolver = new DependencyResolver();
		String appId = resolver.enter(project.getModel(), false);
		
		ResolveHelper resolveHelper =
			new ResolveHelper(session, repositorySystem, pomRemoteRepositories, artifactResolver, artifactHandlerManager);

		Artifact appFragment = resolveHelper.resolveWebFragment(project.getArtifact());
		tasks.put(appId, new AppOverlay(project, appFragment, split(metaConfExcludes), split(metaConfAddons)));
		fragmentById.put(appId, appFragment);

		DefaultModelReader analyzer = new DefaultModelReader();

		for (Artifact dependency : project.getArtifacts()) {
			if ("jar".equals(dependency.getType())) {
				File file = dependency.getFile();
				if (file == null) {
					getLog().info("Missing artifact: " + dependency);
					continue;
				}
				try {
					if (!EngineResolver.isWebComponent(file)) {
						continue;
					}
				} catch (IOException ex) {
					getLog().error("Failed to access '" + file + "': " + ex.getMessage(), ex);
					continue;
				}

				Artifact webFragment = resolveHelper.resolveWebFragment(dependency);
				Artifact pom = resolveHelper.resolve(dependency, "pom", null);
				try {
					Model dependencyModel = analyzer.read(pom.getFile(), null);
					String id = resolver.enter(dependencyModel, false);

					tasks.put(id, new FragmentOverlay(webFragment));
					fragmentById.put(id, webFragment);
				} catch (IOException ex) {
					throw error("Failed to access project model of '" + dependency + "'", ex);
				}
			}
		}
		List<String> buildOrder = resolver.createBuildOrder();
		
		List<Path> resourcePath = new ArrayList<>();
		for (Path fragment : buildOrder.stream()
			.map(fragmentById::get)
			.filter(Objects::nonNull)
			.map(a -> a.getFile().toPath())
			.collect(Collectors.toList())) {
			for (Path root : FileSystems.newFileSystem(fragment).getRootDirectories()) {
				resourcePath.add(root);
			}
		}

		List<WarTask> warTasks =
			buildOrder.stream().map(tasks::get).filter(Objects::nonNull).collect(Collectors.toList());

		for (Overlay overlay : overlays) {
			if (overlay.isSkip()) {
				continue;
			}
			if (overlay.getDeployAspects() != null) {
				for (String deployAspect : overlay.getDeployAspects().split(",")) {
					deployAspect = deployAspect.trim();
					if (deployAspect.isEmpty()) {
						continue;
					}

					// Check whether the "deploy folder" looks like an artifact reference. This
					// allows to specify deploy artifacts to be referenced in a simple property.
					int groupSeparator = deployAspect.indexOf(':');
					if (groupSeparator >= 0) {
						Overlay ref = new Overlay();
						ref.setGroupId(deployAspect.substring(0, groupSeparator));
						String suffix = deployAspect.substring(groupSeparator + 1);
						int artifactIdSeparator = suffix.indexOf(':');
						if (artifactIdSeparator >= 0) {
							ref.setArtifactId(suffix.substring(0, artifactIdSeparator));
							ref.setClassifier(suffix.substring(artifactIdSeparator + 1));
						} else {
							ref.setArtifactId(suffix);
						}
						ref.setType("war");
						addArtifactOverlay(resourcePath, warTasks, ref);
					} else {
						addDeployOverlay(resourcePath, warTasks, deployAspect);
					}
				}
			} else if (overlay.getPath() != null) {
				addPathOverlay(resourcePath, warTasks, overlay.getPath());
			} else {
				addArtifactOverlay(resourcePath, warTasks, overlay);
			}
		}
		
		Collections.reverse(resourcePath);

		WarContext context = new WarContext(getLog(), getWebappDirectory().toPath(), resourcePath);
		
		for (WarTask task : warTasks) {
			task.run(context);
		}

		createLayouts(resolveHelper, resourcePath);
	}

	private String[] split(String commaSeparatedStrings) {
		if (commaSeparatedStrings == null || commaSeparatedStrings.isEmpty()) {
			return new String[0];
		}
		return Arrays.asList(commaSeparatedStrings.split(","))
			.stream()
			.map(s -> s.trim())
			.collect(Collectors.toList())
			.toArray(new String[0]);
	}

	private void addDeployOverlay(List<Path> resourcePath, List<WarTask> warTasks, String deployFolder) {
		File deployDir = new File(getDeployDirectory(), deployFolder);
		if (deployDir.exists()) {
			File resourceDir = getDeployResourceDir(deployDir);
			addPathOverlay(resourcePath, warTasks, resourceDir);
		} else {
			getLog().info("Deploy folder does not exist: " + deployDir.getAbsolutePath());
		}
	}

	private void addPathOverlay(List<Path> resourcePath, List<WarTask> warTasks, File resourceDir) {
		if (resourceDir.exists()) {
			resourcePath.add(resourceDir.toPath());
			warTasks.add(new DeployOverlay(resourceDir.toPath()));
		} else {
			getLog().info("Web app overlay resources do not exist: " + resourceDir.getAbsolutePath());
		}
	}

	private File getDeployResourceDir(File deployDir) {
		String webapp = getDeployWebapp();
		if (webapp == null || webapp.isEmpty()) {
			return deployDir;
		}

		return new File(deployDir, webapp);
	}

	private void addArtifactOverlay(List<Path> resourcePath, List<WarTask> warTasks, Overlay overlay)
			throws IOException {
		Artifact deployArtifact = project.getArtifacts().stream().filter(overlay::matches).findFirst().orElse(null);
		if (deployArtifact != null) {
			for (Path root : FileSystems.newFileSystem(deployArtifact.getFile().toPath()).getRootDirectories()) {
				resourcePath.add(root);
			}
			warTasks.add(new FragmentOverlay(deployArtifact));
		} else {
			getLog().error("Overlay not found in project dependencies: " + overlay.id());
		}
	}

	private void createLayouts(ResolveHelper resolveHelper, List<Path> resourcePath)
			throws MalformedURLException, MojoExecutionException {
		ClassLoader appClassLoader = appClassLoader(resolveHelper, project);
		Thread.currentThread().setContextClassLoader(appClassLoader);

		try {
			Class<?> clazz = appClassLoader.loadClass("com.top_logic.layout.component.LayoutCreator");
			clazz.getMethod("createLayouts", List.class, File.class).invoke(null, resourcePath, webappDirectory);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exception) {
			throw error("Could not create layouts for the application war.", exception);
		}
	}

	/**
	 * Creates a {@link ClassLoader} that is able to load classes of the currently built
	 * application.
	 */
	public static ClassLoader appClassLoader(ResolveHelper resolveHelper, MavenProject project)
			throws MalformedURLException, MojoExecutionException {
		return createClassLoader(getAppClassPath(resolveHelper, project));
	}

	/**
	 * Creates a classloader for the given class path.
	 */
	public static ClassLoader createClassLoader(List<URL> cp) {
		return new URLClassLoader(cp.toArray(new URL[] {}), ClassLoader.getSystemClassLoader());
	}

	/**
	 * Creates a class path of the currently build application.
	 */
	public static List<URL> getAppClassPath(ResolveHelper resolveHelper, MavenProject project)
			throws MalformedURLException, MojoExecutionException {
		List<URL> cp = new ArrayList<>();

		addToClassPath(cp, resolveHelper, project.getArtifact());
		for (Artifact dependency : project.getArtifacts()) {
			if ("jar".equals(dependency.getType())) {
				addToClassPath(cp, resolveHelper, dependency);
			}
		}

		return cp;
	}

	/**
	 * Adds the JAR represented by the given {@link Artifact} to the given classpath.
	 */
	public static void addToClassPath(List<URL> cp, ResolveHelper resolveHelper, Artifact dependency)
			throws MalformedURLException, MojoExecutionException {
		cp.add(getUrl(resolveHelper, dependency));
	}

	/**
	 * Creates an {@link URL} for loading the given artifact file.
	 */
	public static URL getUrl(ResolveHelper resolveHelper, Artifact artifact)
			throws MalformedURLException, MojoExecutionException {
		File file = artifact.getFile();
		if (file == null) {
			Artifact resolvedArtifact = resolveHelper.resolve(artifact, artifact.getType(), artifact.getClassifier());
			file = resolvedArtifact.getFile();
		}
		return file.toURI().toURL();
	}

	private MojoRuntimeException error(String message) {
		throw error(message, null);
	}

	private MojoRuntimeException error(String message, Throwable ex) {
		throw new MojoRuntimeException(message, ex);
	}

	private void ensureOutputDirectory() {
		File f = outputDirectory;
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	private void cleanWebapp() {
		if (getWebappDirectory().exists()) {
			deleteRecursive(getWebappDirectory());
		}
		getWebappDirectory().mkdirs();
	}

	private void deleteRecursive(File file) {
		if (file.isDirectory()) {
			for (File content : file.listFiles()) {
				deleteRecursive(content);
			}
		}
		file.delete();
	}

}
