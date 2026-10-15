/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.generate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.build.maven.war.AppModuleStack;
import com.top_logic.build.maven.war.ResolveHelper;
import com.top_logic.build.maven.war.TLAppWar;

/**
 * Maven goal to generate a Java binding for a TopLogic model definition.
 */
@Mojo(name = "generate-java", requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM, inheritByDefault = false, requiresDependencyCollection = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class GenerateJavaBinding extends AbstractMojo {

	/**
	 * Name of the generator class in the application class path.
	 */
	private static final String GENERATOR_CLASS = "com.top_logic.element.model.generate.WrapperGenerator";

	/**
	 * Name of the static generator method that takes the resource stack and the generator
	 * arguments.
	 */
	private static final String GENERATOR_METHOD = "generate";

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	private MavenProjectHelper projectHelper;

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	private List<ArtifactRepository> pomRemoteRepositories;

	@Component
	private ArtifactResolver artifactResolver;

	@Component
	private ArtifactHandlerManager artifactHandlerManager;

	@Component
	protected ArtifactFactory artifactFactory;

	/**
	 * The directory to generate Java source files to.
	 */
	@Parameter(defaultValue = "${project.build.sourceDirectory}")
	private String outputDirectory;

	/**
	 * Comma separated list of TopLogic module to generate a Java binding for.
	 */
	@Parameter(required = true)
	private String modules;

	/**
	 * Comma separated list of languages to generate translations for.
	 */
	@Parameter(required = false)
	private String languages;

	/**
	 * The copyright header that is used for generated files.
	 * 
	 * <p>
	 * If this configuration is set, the configuration {@link #copyrightHolder} is ignored.
	 * </p>
	 */
	@Parameter
	private String copyrightHeader;

	/**
	 * The copyright holder for the generated classes.
	 */
	@Parameter
	private String copyrightHolder;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ResolveHelper resolveHelper =
			new ResolveHelper(session, repositorySystem, pomRemoteRepositories, artifactResolver,
				artifactHandlerManager);

		try {
			if (copyrightHolder != null) {
				System.setProperty("tl_copyright_holder", copyrightHolder);
			}
			if (copyrightHeader != null) {
				System.setProperty("tl_copyright_header", copyrightHeader);
			}
			List<URL> cp = new ArrayList<>();
			File classesDir = new File(project.getBuild().getOutputDirectory());
			if (classesDir.isDirectory()) {
				cp.add(classesDir.toURI().toURL());
			}
			TLAppWar.addDependenciesToClassPath(cp, resolveHelper, project);
			
			// Add hard coded profile with otherwise provided classes.
			TLAppWar.addToClassPath(cp, resolveHelper,
				artifactFactory.createArtifact("jakarta.servlet", "jakarta.servlet-api", "6.0.0", "runtime", "jar"));

			TLAppWar.addToClassPath(cp, resolveHelper,
				artifactFactory.createArtifact("jakarta.servlet.jsp", "jakarta.servlet.jsp-api", "3.1.0", "runtime", "jar"));

			ClassLoader classLoader = TLAppWar.createClassLoader(cp);

			try (AppModuleStack moduleStack = AppModuleStack.resolve(project, resolveHelper, getLog(), false)) {
				List<Path> resourceStack = moduleStack.resourceStack(appLayer());
				logResourceStack(resourceStack);

				Thread currentThread = Thread.currentThread();
				ClassLoader before = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(classLoader);
				try {
					Class<?> tool = classLoader.loadClass(GENERATOR_CLASS);
					Method generate = tool.getMethod(GENERATOR_METHOD, List.class, String[].class);

					ArrayList<String> mainArgs = new ArrayList<>();
					addOutpuDirectory(mainArgs);
					addResourcesPath(mainArgs);
					addModules(mainArgs);
					addLanguages(mainArgs);

					generate.invoke(null, resourceStack, mainArgs.toArray(new String[0]));
				} finally {
					currentThread.setContextClassLoader(before);
				}
			}
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new MojoFailureException("Generation for modules '" + modules + "' failed.", ex);
		}
	}

	/**
	 * The resource roots of the project itself: its web application directory, if it exists.
	 */
	private List<Path> appLayer() {
		File webapp = new File(project.getBasedir(), ModuleLayoutConstants.WEBAPP_DIR);
		if (webapp.isDirectory()) {
			return List.of(webapp.toPath());
		} else {
			return List.of();
		}
	}

	private void logResourceStack(List<Path> resourceStack) {
		getLog().info("Resource stack (highest priority first):");
		for (Path entry : resourceStack) {
			FileSystem fileSystem = entry.getFileSystem();
			if (fileSystem == FileSystems.getDefault()) {
				getLog().info("  " + entry);
			} else {
				getLog().info("  " + fileSystem + "!" + entry);
			}
		}
	}

	private void addOutpuDirectory(List<String> mainArgs) {
		mainArgs.add("-out");
		mainArgs.add(outputDirectory);
	}

	private void addResourcesPath(List<String> mainArgs) throws IOException {
		File basedir = project.getBasedir();
		File webapp = new File(basedir, ModuleLayoutConstants.WEBAPP_DIR);
		File resourcesDir = new File(webapp, ModuleLayoutConstants.RESOURCES_PATH);
		mainArgs.add("-resources-path");
		mainArgs.add(resourcesDir.getCanonicalPath());
	}

	private void addModules(List<String> mainArgs) {
		mainArgs.add("-modules");
		mainArgs.add(modules);
	}

	private void addLanguages(List<String> mainArgs) {
		if (languages != null) {
			mainArgs.add("-languages");
			mainArgs.add(languages);
		}
	}
}
