/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.maven.generate;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.junit.Test;

import com.top_logic.build.maven.generate.GenerateJavaBinding;

/**
 * Integration test of {@link GenerateJavaBinding} in the build of an application.
 *
 * <p>
 * The test builds the application project {@value #PROJECT} up to {@value #GOAL} in a separate
 * Maven process. The application binds the goal to the phase <code>generate-sources</code> and
 * has never been installed. The build starts with an empty local repository and gets the plugin and
 * the other TopLogic artifacts from the local repository of the running build. The module of this
 * test depends on the plugin and on the engine modules of the application, so that the running
 * build has installed them before the test runs.
 * </p>
 */
public class TestGenerateJavaIntegration {

	/**
	 * System property with the Maven installation that runs the application build.
	 */
	private static final String PROPERTY_MAVEN_HOME = "tl.it.mavenHome";

	/**
	 * System property with the local repository of the running build, the source of the remote
	 * repositories of the application build.
	 */
	private static final String PROPERTY_REMOTE_REPOSITORY = "tl.it.remoteRepository";

	/**
	 * System property telling whether the local repository of the running build is split into a
	 * part with the installed and a part with the downloaded artifacts.
	 *
	 * <p>
	 * A split repository has no artifacts in its root: the application build gets the two parts
	 * {@link #PROPERTY_LOCAL_PREFIX} and {@link #PROPERTY_REMOTE_PREFIX} as remote repositories.
	 * </p>
	 */
	private static final String PROPERTY_REPOSITORY_SPLIT = "tl.it.repositorySplit";

	/**
	 * System property with the directory of the installed artifacts in a split local repository of
	 * the running build, relative to its root.
	 */
	private static final String PROPERTY_LOCAL_PREFIX = "tl.it.localPrefix";

	/**
	 * System property with the directory of the downloaded artifacts in a split local repository of
	 * the running build, relative to its root.
	 */
	private static final String PROPERTY_REMOTE_PREFIX = "tl.it.remotePrefix";

	/**
	 * Maven user property that splits the local repository of a build.
	 *
	 * <p>
	 * The application build runs with an unsplit local repository, also when the running build
	 * passes a split one in <code>MAVEN_ARGS</code>: the test checks the artifacts at their plain
	 * paths.
	 * </p>
	 */
	private static final String MAVEN_REPOSITORY_SPLIT = "aether.enhancedLocalRepository.split";

	/**
	 * System property with the version of the plugin under test and the engine.
	 */
	private static final String PROPERTY_VERSION = "tl.it.version";

	/**
	 * System property with the directory containing the test projects.
	 */
	private static final String PROPERTY_PROJECTS = "tl.it.projects";

	/**
	 * System property with the directory the test builds in.
	 */
	private static final String PROPERTY_WORK_DIR = "tl.it.workDir";

	/**
	 * Placeholder for the version of the plugin and the engine in the POM of the test project.
	 */
	private static final String VERSION_PLACEHOLDER = "@project.version@";

	/**
	 * ID of the profile in {@link #SETTINGS} that declares the remote repository.
	 */
	private static final String REPOSITORY_PROFILE = "it-repo";

	/**
	 * Group ID of the TopLogic artifacts, as path in a repository.
	 */
	private static final String TL_GROUP_PATH = "com/top-logic";

	/**
	 * Name of the test project.
	 */
	private static final String PROJECT = "generate-java-it";

	/**
	 * The goal of the application build.
	 */
	private static final String GOAL = "test-compile";

	/**
	 * Directory of the Maven configuration of a multi-module root.
	 */
	private static final String MAVEN_CONFIG_DIR = ".mvn";

	/**
	 * Repository path of the compiler plugin, which every build of a Java project uses.
	 *
	 * <p>
	 * The plugin is in the empty local repository after the build only if the build resolved into
	 * that repository alone, without reading from a further repository such as a
	 * <code>maven.repo.local.tail</code>.
	 * </p>
	 */
	private static final String COMPILER_PLUGIN_PATH = "org/apache/maven/plugins/maven-compiler-plugin";

	/**
	 * Log message of the plugin for a web fragment it could not resolve.
	 */
	private static final String SKIPPED_FRAGMENT = "Skipping web fragment";

	/**
	 * Global settings of the application build, a format with the {@link #REPOSITORY} entries of
	 * the repositories, the entries of the plugin repositories, and {@link #REPOSITORY_PROFILE} as
	 * arguments.
	 *
	 * <p>
	 * The settings add the local repository of the running build as remote repositories, so that
	 * the application build uses the engine of the running build. They are passed as global
	 * settings, so that the user settings of the running build (mirrors, proxies, credentials)
	 * apply as well.
	 * </p>
	 */
	private static final String SETTINGS = """
		<?xml version="1.0" encoding="UTF-8"?>
		<settings>
			<profiles>
				<profile>
					<id>%3$s</id>
					<repositories>
		%1$s			</repositories>
					<pluginRepositories>
		%2$s			</pluginRepositories>
				</profile>
			</profiles>
			<activeProfiles>
				<activeProfile>%3$s</activeProfile>
			</activeProfiles>
		</settings>
		""";

	/**
	 * Entry of a repository in {@link #SETTINGS}, a format with the element name
	 * (<code>repository</code> or <code>pluginRepository</code>), the ID and the URL as arguments.
	 */
	private static final String REPOSITORY = """
						<%1$s>
							<id>%2$s</id>
							<url>%3$s</url>
							<releases><enabled>true</enabled></releases>
							<snapshots><enabled>true</enabled></snapshots>
						</%1$s>
		""";

	/**
	 * Prefix of the IDs of the repositories in {@link #SETTINGS}.
	 */
	private static final String REPOSITORY_ID = "local.central";

	/**
	 * The generated wrapper classes of the model module <code>it.app</code>.
	 */
	private static final List<String> GENERATED_CLASSES =
		List.of("it/app/Item", "it/app/ItAppFactory", "it/app/impl/ItemBase", "it/app/impl/ItemImpl");

	/**
	 * Tests that the application build generates the binding of its model in
	 * <code>generate-sources</code>, resolves the web fragments of the engine modules into the
	 * empty local repository, and compiles the generated binding.
	 */
	@Test
	public void testGenerateInNeverInstalledApplication() throws IOException, MavenInvocationException {
		String version = property(PROPERTY_VERSION);
		Path workDir = Path.of(property(PROPERTY_WORK_DIR));
		deleteRecursively(workDir);

		Path project = workDir.resolve(PROJECT);
		copyProject(Path.of(property(PROPERTY_PROJECTS)).resolve(PROJECT), project, version);

		// Makes the project its own multi-module root: Maven does not read the Maven configuration
		// of an enclosing directory.
		Files.createDirectory(project.resolve(MAVEN_CONFIG_DIR));

		Path repository = workDir.resolve("local-repo");
		Files.createDirectories(repository);

		Path settings = workDir.resolve("settings.xml");
		Files.writeString(settings, settings(remoteRepositories()), StandardCharsets.UTF_8);

		Path log = workDir.resolve("build.log");
		int exitCode = build(project, repository, settings, log);
		String output = Files.readString(log, StandardCharsets.UTF_8);

		assertEquals("Build of the application failed, see " + log, 0, exitCode);
		for (String name : GENERATED_CLASSES) {
			Path source = project.resolve("src/main/java/" + name + ".java");
			assertTrue("Not generated: " + source, Files.isRegularFile(source));
			Path compiled = project.resolve("target/classes/" + name + ".class");
			assertTrue("Not compiled: " + compiled, Files.isRegularFile(compiled));
		}
		assertFalse("A web fragment of an engine module was not resolved, see " + log,
			output.contains(SKIPPED_FRAGMENT));
		Path coreFragment = repository.resolve(TL_GROUP_PATH + "/tl-core/" + version + "/tl-core-" + version
			+ "-web-fragment.war");
		assertTrue("Web fragment of tl-core not resolved: " + coreFragment, Files.isRegularFile(coreFragment));
		Path compilerPlugin = repository.resolve(COMPILER_PLUGIN_PATH);
		assertTrue("The build did not resolve into its own local repository: " + compilerPlugin + " missing.",
			Files.isDirectory(compilerPlugin));
	}

	/**
	 * The directories of the local repository of the running build that hold artifacts: the root
	 * of a plain repository, the installed and the downloaded part of a split one.
	 */
	private static List<Path> remoteRepositories() {
		Path root = Path.of(property(PROPERTY_REMOTE_REPOSITORY));
		if (!Boolean.parseBoolean(property(PROPERTY_REPOSITORY_SPLIT))) {
			return List.of(root);
		}
		return List.of(root.resolve(property(PROPERTY_LOCAL_PREFIX)), root.resolve(property(PROPERTY_REMOTE_PREFIX)));
	}

	/**
	 * The {@link #SETTINGS} declaring the given directories as repositories and plugin
	 * repositories.
	 */
	private static String settings(List<Path> remoteRepositories) {
		StringBuilder repositories = new StringBuilder();
		StringBuilder pluginRepositories = new StringBuilder();
		for (int n = 0; n < remoteRepositories.size(); n++) {
			String id = n == 0 ? REPOSITORY_ID : REPOSITORY_ID + "." + n;
			String url = remoteRepositories.get(n).toUri().toString();
			repositories.append(REPOSITORY.formatted("repository", id, url));
			pluginRepositories.append(REPOSITORY.formatted("pluginRepository", id, url));
		}
		return SETTINGS.formatted(repositories, pluginRepositories, REPOSITORY_PROFILE);
	}

	private static int build(Path project, Path repository, Path settings, Path log)
			throws IOException, MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setBaseDirectory(project.toFile());
		request.setGoals(List.of(GOAL));
		request.setBatchMode(true);
		request.setGlobalSettingsFile(settings.toFile());
		request.setLocalRepositoryDirectory(repository.toFile());
		Properties properties = new Properties();
		properties.setProperty(MAVEN_REPOSITORY_SPLIT, Boolean.FALSE.toString());
		request.setProperties(properties);

		DefaultInvoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(property(PROPERTY_MAVEN_HOME)));
		try (PrintStream out = new PrintStream(Files.newOutputStream(log), true, StandardCharsets.UTF_8)) {
			PrintStreamHandler handler = new PrintStreamHandler(out, true);
			request.setOutputHandler(handler);
			request.setErrorHandler(handler);
			InvocationResult result = invoker.execute(request);
			if (result.getExecutionException() != null) {
				throw new AssertionError("Maven could not be started.", result.getExecutionException());
			}
			return result.getExitCode();
		}
	}

	/**
	 * Copies the test project and inserts the version of the plugin and the engine into its POM.
	 */
	private static void copyProject(Path source, Path target, String version) throws IOException {
		try (Stream<Path> files = Files.walk(source)) {
			for (Path file : (Iterable<Path>) files::iterator) {
				Path copy = target.resolve(source.relativize(file).toString());
				if (Files.isDirectory(file)) {
					Files.createDirectories(copy);
				} else {
					Files.copy(file, copy);
				}
			}
		}
		Path pom = target.resolve("pom.xml");
		String content = Files.readString(pom, StandardCharsets.UTF_8);
		Files.writeString(pom, content.replace(VERSION_PLACEHOLDER, version), StandardCharsets.UTF_8);
	}

	private static void deleteRecursively(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path file : (Iterable<Path>) files.sorted(Comparator.reverseOrder())::iterator) {
				Files.delete(file);
			}
		}
	}

	private static String property(String name) {
		String value = System.getProperty(name);
		assertNotNull("System property '" + name + "' not set, the test runs only in the Maven build.", value);
		return value;
	}

}
