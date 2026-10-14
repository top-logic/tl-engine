/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.maven.generate;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
 * has never been installed. The build starts with an empty local repository whose only remote is
 * the local repository of the running build. The plugin under test is staged into the empty
 * repository from the plugin's POM and compiled classes.
 * </p>
 */
public class TestGenerateJavaIntegration {

	/**
	 * System property with the Maven installation that runs the application build.
	 */
	private static final String PROPERTY_MAVEN_HOME = "tl.it.mavenHome";

	/**
	 * System property with the local repository of the running build, the remote repository of the
	 * application build.
	 */
	private static final String PROPERTY_REMOTE_REPOSITORY = "tl.it.remoteRepository";

	/**
	 * System property with the version of the plugin under test.
	 */
	private static final String PROPERTY_PLUGIN_VERSION = "tl.it.pluginVersion";

	/**
	 * System property with the POM file of the plugin under test.
	 */
	private static final String PROPERTY_PLUGIN_POM = "tl.it.pluginPom";

	/**
	 * System property with the compiled classes of the plugin under test.
	 */
	private static final String PROPERTY_PLUGIN_CLASSES = "tl.it.pluginClasses";

	/**
	 * System property with the directory containing the test projects.
	 */
	private static final String PROPERTY_PROJECTS = "tl.it.projects";

	/**
	 * System property with the directory the test builds in.
	 */
	private static final String PROPERTY_WORK_DIR = "tl.it.workDir";

	/**
	 * Placeholder for the plugin version in the POM of the test project.
	 */
	private static final String VERSION_PLACEHOLDER = "@project.version@";

	/**
	 * Placeholder for the URL of the remote repository in {@link #SETTINGS}.
	 */
	private static final String URL_PLACEHOLDER = "@remoteRepositoryUrl@";

	/**
	 * Group ID of the plugin under test, as path in a repository.
	 */
	private static final String PLUGIN_GROUP_PATH = "com/top-logic";

	/**
	 * Artifact ID of the plugin under test.
	 */
	private static final String PLUGIN_ARTIFACT = "tl-maven-plugin";

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
	 * Settings of the application build.
	 *
	 * <p>
	 * The only remote for TopLogic artifacts is the local repository of the running build: the
	 * snapshot repository the engine POMs declare is disabled, so the build uses the engine of the
	 * running build.
	 * </p>
	 */
	private static final String SETTINGS = """
		<?xml version="1.0" encoding="UTF-8"?>
		<settings>
			<profiles>
				<profile>
					<id>it-repo</id>
					<activation>
						<activeByDefault>true</activeByDefault>
					</activation>
					<repositories>
						<repository>
							<id>local.central</id>
							<url>@remoteRepositoryUrl@</url>
							<releases><enabled>true</enabled></releases>
							<snapshots><enabled>true</enabled></snapshots>
						</repository>
						<repository>
							<id>tl-snapshots</id>
							<url>https://dev.top-logic.com/nexus/repository/toplogic-snapshots/</url>
							<releases><enabled>false</enabled></releases>
							<snapshots><enabled>false</enabled></snapshots>
						</repository>
					</repositories>
					<pluginRepositories>
						<pluginRepository>
							<id>local.central</id>
							<url>@remoteRepositoryUrl@</url>
							<releases><enabled>true</enabled></releases>
							<snapshots><enabled>true</enabled></snapshots>
						</pluginRepository>
						<pluginRepository>
							<id>tl-snapshots</id>
							<url>https://dev.top-logic.com/nexus/repository/toplogic-snapshots/</url>
							<releases><enabled>false</enabled></releases>
							<snapshots><enabled>false</enabled></snapshots>
						</pluginRepository>
					</pluginRepositories>
				</profile>
			</profiles>
		</settings>
		""";

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
		String version = property(PROPERTY_PLUGIN_VERSION);
		Path workDir = Path.of(property(PROPERTY_WORK_DIR));
		deleteRecursively(workDir);

		Path project = workDir.resolve(PROJECT);
		copyProject(Path.of(property(PROPERTY_PROJECTS)).resolve(PROJECT), project, version);

		// Makes the project its own multi-module root: Maven does not read the Maven configuration
		// of an enclosing directory.
		Files.createDirectory(project.resolve(MAVEN_CONFIG_DIR));

		Path repository = workDir.resolve("local-repo");
		Files.createDirectories(repository);
		stagePlugin(repository, version);

		Path settings = workDir.resolve("settings.xml");
		String remoteUrl = Path.of(property(PROPERTY_REMOTE_REPOSITORY)).toUri().toString();
		Files.writeString(settings, SETTINGS.replace(URL_PLACEHOLDER, remoteUrl), StandardCharsets.UTF_8);

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
		Path coreFragment = repository.resolve(PLUGIN_GROUP_PATH + "/tl-core/" + version + "/tl-core-" + version
			+ "-web-fragment.war");
		assertTrue("Web fragment of tl-core not resolved: " + coreFragment, Files.isRegularFile(coreFragment));
		Path compilerPlugin = repository.resolve(COMPILER_PLUGIN_PATH);
		assertTrue("The build did not resolve into its own local repository: " + compilerPlugin + " missing.",
			Files.isDirectory(compilerPlugin));
	}

	private static int build(Path project, Path repository, Path settings, Path log)
			throws IOException, MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setBaseDirectory(project.toFile());
		request.setGoals(List.of(GOAL));
		request.setBatchMode(true);
		request.setUserSettingsFile(settings.toFile());
		request.setLocalRepositoryDirectory(repository.toFile());

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
	 * Copies the test project and inserts the version of the plugin under test into its POM.
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

	/**
	 * Puts the plugin under test into the given repository: its POM and a jar of its compiled
	 * classes.
	 */
	private static void stagePlugin(Path repository, String version) throws IOException {
		Path classes = Path.of(property(PROPERTY_PLUGIN_CLASSES));
		assertTrue("Plugin descriptor missing in " + classes,
			Files.isRegularFile(classes.resolve("META-INF/maven/plugin.xml")));

		Path dir = repository.resolve(PLUGIN_GROUP_PATH + "/" + PLUGIN_ARTIFACT + "/" + version);
		Files.createDirectories(dir);
		String baseName = PLUGIN_ARTIFACT + "-" + version;
		Files.copy(Path.of(property(PROPERTY_PLUGIN_POM)), dir.resolve(baseName + ".pom"));
		try (OutputStream out = Files.newOutputStream(dir.resolve(baseName + ".jar"));
				ZipOutputStream zip = new ZipOutputStream(out);
				Stream<Path> files = Files.walk(classes)) {
			for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
				zip.putNextEntry(new ZipEntry(classes.relativize(file).toString().replace(File.separatorChar, '/')));
				Files.copy(file, zip);
				zip.closeEntry();
			}
		}
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
