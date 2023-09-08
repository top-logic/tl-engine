/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;

/**
 * Utility for resolving artifacts.
 */
public class ResolveHelper {

	private MavenSession session;

	private RepositorySystem repositorySystem;

	private List<ArtifactRepository> pomRemoteRepositories;

	private ArtifactResolver artifactResolver;

	private ArtifactHandlerManager artifactHandlerManager;

	/**
	 * Creates a {@link ResolveHelper}.
	 */
	public ResolveHelper(MavenSession session, RepositorySystem repositorySystem,
			List<ArtifactRepository> pomRemoteRepositories, ArtifactResolver artifactResolver,
			ArtifactHandlerManager artifactHandlerManager) {
		this.session = session;
		this.repositorySystem = repositorySystem;
		this.pomRemoteRepositories = pomRemoteRepositories;
		this.artifactResolver = artifactResolver;
		this.artifactHandlerManager = artifactHandlerManager;
	}

	/**
	 * Resolves the adapted {@link Artifact} with given type and given classifier.
	 * 
	 * @param base
	 *        Base {@link Artifact} to get group id, artifact id and version for result artifact.
	 * @param type
	 *        Type of the result artifact.
	 * @param classifier
	 *        Classifier for the result artifact. May be <code>null</code>.
	 * @return The resolved {@link Artifact}.
	 */
	public Artifact resolve(Artifact base, String type, String classifier) throws MojoExecutionException {
		DefaultDependableCoordinate coordinate = new DefaultDependableCoordinate();
		coordinate.setGroupId(base.getGroupId());
		coordinate.setArtifactId(base.getArtifactId());
		coordinate.setVersion(base.getVersion());
		coordinate.setType(type);
		coordinate.setClassifier(classifier);

		ProjectBuildingRequest buildingRequest =
			new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

		Settings settings = session.getSettings();
		repositorySystem.injectMirror(pomRemoteRepositories, settings.getMirrors());
		repositorySystem.injectProxy(pomRemoteRepositories, settings.getProxies());
		repositorySystem.injectAuthentication(pomRemoteRepositories, settings.getServers());

		buildingRequest.setRemoteRepositories(pomRemoteRepositories);

		try {
			ArtifactResult result = artifactResolver.resolveArtifact(buildingRequest, toArtifactCoordinate(coordinate));
			return result.getArtifact();
		} catch (ArtifactResolverException ex) {
			throw new MojoExecutionException("Failed to resolve dependency '" + coordinate + "': " + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Resolves the web fragment {@link Artifact} for the given base artifact, i.e. the artifact
	 * with type "war" and classifier "web-fragment"
	 * 
	 * @see #resolve(Artifact, String, String)
	 */
	public Artifact resolveWebFragment(Artifact base) throws MojoExecutionException {
		return resolve(base, "war", "tests".equals(base.getClassifier()) ? "tests-web-fragment" : "web-fragment");
	}

	private ArtifactCoordinate toArtifactCoordinate(DependableCoordinate dependableCoordinate) {
		ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(dependableCoordinate.getType());
		DefaultArtifactCoordinate artifactCoordinate = new DefaultArtifactCoordinate();
		artifactCoordinate.setGroupId(dependableCoordinate.getGroupId());
		artifactCoordinate.setArtifactId(dependableCoordinate.getArtifactId());
		artifactCoordinate.setVersion(dependableCoordinate.getVersion());
		artifactCoordinate.setClassifier(dependableCoordinate.getClassifier());
		artifactCoordinate.setExtension(artifactHandler.getExtension());
		return artifactCoordinate;
	}

}
