/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.resolve;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.ProviderNotFoundException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import com.top_logic.build.maven.war.ResolveHelper;

/**
 * Goal to resolve all required artifacts of the TopLogic engine for a certain application.
 * 
 * <p>
 * This goal has to be executed once before development of a newly set up application can start. It
 * must be re-executed after new engine dependencies are added to an application to ensure these
 * components are downloaded into the local repository.
 * </p>
 */
@Mojo(name = "resolve", defaultPhase = LifecyclePhase.TEST_COMPILE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class EngineResolver extends AbstractMojo {

	private static final String TL_MODULE_MARKER = "/META-INF/tl-module-with-resources";

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	private List<ArtifactRepository> pomRemoteRepositories;

	@Component
	private ArtifactResolver artifactResolver;

	@Component
	private ArtifactHandlerManager artifactHandlerManager;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ResolveHelper resolveHelper =
			new ResolveHelper(session, repositorySystem, pomRemoteRepositories, artifactResolver,
				artifactHandlerManager);

		for (Artifact dependency : project.getArtifacts()) {
			if ("jar".equals(dependency.getType()) || "test-jar".equals(dependency.getType())) {
				File file = dependency.getFile();
				if (file == null) {
					getLog().info("Missing artifact: " + dependency);
					continue;
				}
				try {
					if (isWebComponent(file)) {
						resolveHelper.resolveWebFragment(dependency);
					}
				} catch (IOException | ProviderNotFoundException ex) {
					getLog().error("Failed to access '" + file + "': " + ex.getMessage(), ex);
					continue;
				}
			}
		}
	}

	/**
	 * Checks whether the given file is a TopLogic web component jar.
	 */
	public static boolean isWebComponent(File file) throws IOException, ProviderNotFoundException {
		if (file.isDirectory()) {
			return new File(file, TL_MODULE_MARKER).exists();
		} else {
			try (FileSystem jar = FileSystems.newFileSystem(file.toPath())) {
				return Files.exists(jar.getPath(TL_MODULE_MARKER));
			}
		}
	}

}
